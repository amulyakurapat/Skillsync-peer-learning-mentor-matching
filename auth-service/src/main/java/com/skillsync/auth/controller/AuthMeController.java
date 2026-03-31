package com.skillsync.auth.controller;

import com.skillsync.auth.dto.UserProfileResponse;
import com.skillsync.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: AuthMeController
 * DESCRIPTION:
 * Fallback controller for the '/me' endpoint to ensure profile 
 * fetching works even when gateway prefix stripping occurs.
 * ================================================================
 */
@RestController
@Tag(name = "Auth Service (fallback)", description = "Endpoints to support gateway prefix stripping")
@io.swagger.v3.oas.annotations.Hidden
public class AuthMeController {

    /*
     * Logger instance for fallback endpoint monitoring
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthMeController.class);

    @Autowired
    private AuthService authService;

    /* ================================================================
     * METHOD: getMe
     * DESCRIPTION:
     * Fallback method for retrieving current user profile.
     * ================================================================ */
    @io.swagger.v3.oas.annotations.Hidden
    @GetMapping("/me")
    @Operation(summary = "Get current logged in user profile (fallback path)")
    public ResponseEntity<UserProfileResponse> getMe(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        logger.info("Fallback '/me' endpoint triggered");
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Fallback profile fetch failed: Authorization header missing");
            throw new RuntimeException("Authorization header missing");
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7).trim()
                : authHeader.trim();

        return ResponseEntity.ok(authService.getMe(token));
    }
}

