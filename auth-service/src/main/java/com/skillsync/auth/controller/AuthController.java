package com.skillsync.auth.controller;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.skillsync.auth.dto.LoginRequest;
import com.skillsync.auth.dto.LoginResponse;
import com.skillsync.auth.dto.RefreshTokenRequest;
import com.skillsync.auth.dto.RefreshTokenResponse;
import com.skillsync.auth.dto.RegisterRequest;
import com.skillsync.auth.dto.UserProfileResponse;
import com.skillsync.auth.dto.ValidateResponse;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.security.JwtUtil;
import com.skillsync.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: AuthController
 * DESCRIPTION:
 * This controller manages authentication and authorization APIs, 
 * including user registration, login, token validation, 
 * profile retrieval, and logout.
 * ================================================================
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Service", description = "Authentication and Authorization APIs")
public class AuthController {

    /*
     * Logger instance for tracking authentication events and security monitoring
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    /* ================================================================
     * METHOD: registerUser
     * DESCRIPTION:
     * Registers a new user account with the platform.
     * ================================================================ */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registering new user: {}", request.getEmail());
        User user = authService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole().name()
        );
        return ResponseEntity.ok(user);
    }

    /* ================================================================
     * METHOD: login
     * DESCRIPTION:
     * Authenticates user credentials and generates JWT access and refresh tokens.
     * ================================================================ */
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        User user = authService.loginUser(request.getEmail(), request.getPassword());
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");
        String accessToken = jwtUtil.generateToken(user.getEmail(), role, user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        logger.info("Login successful for user: {}", user.getEmail());
        return ResponseEntity.ok(new LoginResponse(
                accessToken, refreshToken, user.getId(), user.getEmail(), role));
    }

    /* ================================================================
     * METHOD: validate
     * DESCRIPTION:
     * Validates the provided JWT token through the AuthService.
     * ================================================================ */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<ValidateResponse> validate(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Token validation request received");
        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();
        return ResponseEntity.ok(authService.validateToken(token));
    }

    /* ================================================================
     * METHOD: getMe
     * DESCRIPTION:
     * Returns the profile details of the currently authenticated user.
     * ================================================================ */
    @GetMapping("/me")
    @Operation(summary = "Get current logged in user profile")
    public ResponseEntity<UserProfileResponse> getMe(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Fetching profile for current session");
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Profile fetch failed: Authorization header missing");
            throw new RuntimeException("Authorization header missing");
        }
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();
        return ResponseEntity.ok(authService.getMe(token));
    }

    /* ================================================================
     * METHOD: refresh
     * DESCRIPTION:
     * Generates a new access token using a valid refresh token.
     * ================================================================ */
    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    /* ================================================================
     * METHOD: logout
     * DESCRIPTION:
     * Invalidates the user's current session and token.
     * ================================================================ */
    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate token")
    public ResponseEntity<String> logout(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Logout request received");
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.logout(token));
    }
}