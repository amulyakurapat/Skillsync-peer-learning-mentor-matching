package com.skillsync.skill.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Component
public class JwtRoleFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Call Auth Service to validate token and get role
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://localhost:8081/auth/validate";

                org.springframework.http.HttpHeaders headers =
                        new org.springframework.http.HttpHeaders();
                headers.set("Authorization", "Bearer " + token);

                org.springframework.http.HttpEntity<String> entity =
                        new org.springframework.http.HttpEntity<>(headers);

                org.springframework.http.ResponseEntity<java.util.Map> result =
                        restTemplate.exchange(url,
                                org.springframework.http.HttpMethod.GET,
                                entity,
                                java.util.Map.class);

                java.util.Map body = result.getBody();

                if (body != null && Boolean.TRUE.equals(body.get("valid"))) {
                    String role = (String) body.get("role");

                    // Set Spring Security authentication with role
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    body.get("email"),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" +
                                            role.replace("ROLE_", "")))
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception e) {
                System.out.println("Token validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}