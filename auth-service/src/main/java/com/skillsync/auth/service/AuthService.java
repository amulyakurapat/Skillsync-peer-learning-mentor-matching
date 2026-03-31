package com.skillsync.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.skillsync.auth.dto.UserProfileResponse;
import com.skillsync.auth.dto.ValidateResponse;
import com.skillsync.auth.entity.Role;
import com.skillsync.auth.entity.RoleName;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.exception.InvalidCredentialsException;
import com.skillsync.auth.exception.RoleNotFoundException;
import com.skillsync.auth.exception.UserAlreadyExistsException;
import com.skillsync.auth.repository.RoleRepository;
import com.skillsync.auth.repository.UserRepository;
import com.skillsync.auth.security.JwtUtil;
import com.skillsync.auth.dto.RefreshTokenResponse;
import com.skillsync.auth.client.UserClient;
import com.skillsync.auth.dto.UserDto;
/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: AuthService
 * DESCRIPTION:
 * This service handles the core authentication logic, including 
 * user registration with role management, login verification, 
 * token validation, and session management.
 * ================================================================
 */
@Service
public class AuthService {

    /*
     * Logger instance for internal authentication logic and security auditing
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserClient userClient;

    // ✅ EXISTING — register
    /* ================================================================
     * METHOD: registerUser
     * DESCRIPTION:
     * Registers a new user, hashes the password, assigns roles, 
     * and synchronizes the profile with the User Service.
     * ================================================================ */
    public User registerUser(String name, String email, String password, String roleName) {
        logger.info("Service request: Registering user with email {}", email);
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed: User already exists with email {}", email);
            throw new UserAlreadyExistsException(email);
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        RoleName finalRoleEnum;

        if (roleName == null || roleName.isEmpty()) {
            finalRoleEnum = RoleName.ROLE_LEARNER;
        } else {
            RoleName requestedRole = RoleName.valueOf(roleName);

            if (requestedRole == RoleName.ROLE_ADMIN) {
                logger.error("Security alert: Attempt to register ADMIN role via API for {}", email);
                throw new RuntimeException("Admin cannot be registered via API");
            }

            finalRoleEnum = requestedRole;
        }

        Role role = roleRepository.findByName(finalRoleEnum)
                .orElseThrow(() -> new RoleNotFoundException(finalRoleEnum.name()));
        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        // create DTO for User Service
        UserDto dto = new UserDto();
        dto.setId(savedUser.getId());
        dto.setName(savedUser.getName());
        dto.setEmail(savedUser.getEmail());

        // call User Service
        logger.info("Synchronizing profile for user {} with User Service", email);
        userClient.createUser(dto);

        return savedUser;
    }

    // ✅ EXISTING — login
    /* ================================================================
     * METHOD: loginUser
     * DESCRIPTION:
     * Verifies user credentials and returns the user entity on success.
     * ================================================================ */
    public User loginUser(String email, String password) {
        logger.info("Service request: Authenticating user {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Authentication failed: Wrong password for {}", email);
            throw new InvalidCredentialsException();
        }
        return user;
    }

    // ✅ NEW — validate token
    /* ================================================================
     * METHOD: validateToken
     * DESCRIPTION:
     * Checks if a JWT token is valid and extracts user identity details.
     * ================================================================ */
    public ValidateResponse validateToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return new ValidateResponse(false, null, null, null);
            }
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(InvalidCredentialsException::new);
            String role = user.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse("UNKNOWN");
            return new ValidateResponse(true, user.getId(), user.getEmail(), role);
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return new ValidateResponse(false, null, null, null);
        }
    }

    // ✅ NEW — get current user profile
    /* ================================================================
     * METHOD: getMe
     * DESCRIPTION:
     * Extracts user identity from a valid token and returns profile info.
     * ================================================================ */
    public UserProfileResponse getMe(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), role);
    }
 // ✅ NEW — refresh token
    /* ================================================================
     * METHOD: refreshToken
     * DESCRIPTION:
     * Generates a new access/refresh token pair using a valid refresh token.
     * ================================================================ */
    public RefreshTokenResponse refreshToken(String refreshToken) {
        logger.info("Service request: Refreshing access token");
        if (!jwtUtil.validateToken(refreshToken)) {
            logger.warn("Token refresh failed: Invalid or expired refresh token");
            throw new RuntimeException("Invalid or expired refresh token!");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        // verify user still exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");

        String newAccessToken = jwtUtil.generateToken(email, role, user.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        logger.info("Token refresh successful for user {}", email);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    // ✅ NEW — logout
    /* ================================================================
     * METHOD: logout
     * DESCRIPTION:
     * Logs out the user by verifying the token before session invalidation.
     * ================================================================ */
    public String logout(String token) {
        if (!jwtUtil.validateToken(token)) {
            logger.warn("Logout failed: Invalid token provided");
            throw new RuntimeException("Invalid token!");
        }
        String email = jwtUtil.extractEmail(token);
        logger.info("Service request: User logged out successfully: {}", email);
        return "Logged out successfully!";
    }
}