package com.skillsync.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: JwtUtil
 * DESCRIPTION:
 * Utility class for handling JSON Web Tokens (JWT). Provides 
 * methods for token generation (access & refresh), validation, 
 * and claim extraction.
 * ================================================================
 */
@Component
public class JwtUtil {

    /*
     * Logger instance for JWT operation tracking and security auditing
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtUtil.class);

    private final String SECRET = "c2tpbGxzeW5jc2VjcmV0a2V5c2tpbGxzeW5jc2VjcmV0a2V5";

    /* ================================================================
     * METHOD: getSigningKey
     * DESCRIPTION:
     * Decodes the base64 secret and returns the HMAC signing key.
     * ================================================================ */
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* ================================================================
     * METHOD: generateToken
     * DESCRIPTION:
     * Generates a 24-hour access token with email, role, and user ID.
     * ================================================================ */
    public String generateToken(String email, String role, Long userId) {
        logger.info("Generating new JWT access token for {}", email);
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "role", role,
                        "userId", userId
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ================================================================
     * METHOD: extractEmail
     * DESCRIPTION:
     * Extracts the subject (email) from a valid JWT token.
     * ================================================================ */
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /* ================================================================
     * METHOD: validateToken
     * DESCRIPTION:
     * Verifies the token signature and expiration status.
     * ================================================================ */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    /* ================================================================
     * METHOD: generateRefreshToken
     * DESCRIPTION:
     * Generates a 7-day refresh token for session recovery.
     * ================================================================ */
    public String generateRefreshToken(String email) {
        logger.info("Generating new JWT refresh token for {}", email);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 604800000)) // 7 days
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ================================================================
     * METHOD: extractAllClaims
     * DESCRIPTION:
     * Extracts all claims contained within the token body.
     * ================================================================ */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}