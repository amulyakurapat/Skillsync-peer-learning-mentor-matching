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
@Service
public class AuthService {

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
    public User registerUser(String name, String email, String password, String roleName) {
        if (userRepository.existsByEmail(email)) {
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
     userClient.createUser(dto);

     return savedUser;
    }

    // ✅ EXISTING — login
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    // ✅ NEW — validate token
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
            System.out.println("Validate error: " + e.getMessage());
            return new ValidateResponse(false, null, null, null);
        }
    }

    // ✅ NEW — get current user profile
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
    public RefreshTokenResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token!");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        // verify user still exists
        userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    // ✅ NEW — logout
    public String logout(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token!");
        }
        String email = jwtUtil.extractEmail(token);
        System.out.println("✅ User logged out: " + email);
        return "Logged out successfully!";
    }
}