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
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Service", description = "Authentication and Authorization APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole().name()
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = authService.loginUser(request.getEmail(), request.getPassword());
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");
        return ResponseEntity.ok(new LoginResponse(
                accessToken, refreshToken, user.getId(), user.getEmail(), role));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();

        return ResponseEntity.ok(authService.validateToken(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("Authorization header missing");
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();

        return ResponseEntity.ok(authService.getMe(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate token")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.logout(token));
    }
}