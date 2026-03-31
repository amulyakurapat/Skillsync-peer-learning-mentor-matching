package com.skillsync.gateway.filter;

import com.skillsync.gateway.util.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(1)
public class JwtAuthenticationFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/validate",
            "/auth/refresh",
            "/auth/swagger-ui",
            "/auth/v3/api-docs",
            "/users/swagger-ui",
            "/users/v3/api-docs",
            "/skills/swagger-ui",
            "/skills/v3/api-docs",
            "/mentors/swagger-ui",
            "/mentors/v3/api-docs",
            "/sessions/swagger-ui",
            "/sessions/v3/api-docs",
            "/reviews/swagger-ui",
            "/reviews/v3/api-docs",
            "/notifications/swagger-ui",
            "/notifications/v3/api-docs",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    private static final Pattern USER_ID_PATH = Pattern.compile("^/users/(\\d+)(/.*)?$");
    private static final Pattern MENTOR_STATUS_PATH = Pattern.compile("^/mentors/(\\d+)/status$");
    private static final Pattern MENTOR_AVAILABILITY_PATH = Pattern.compile("^/mentors/(\\d+)/availability$");
    private static final Pattern MENTOR_USER_PATH = Pattern.compile("^/mentors/user/(\\d+)$");
    private static final Pattern REVIEW_LEARNER_PATH = Pattern.compile("^/reviews/learner/(\\d+)$");

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        // Never block CORS preflight.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Public mentor discovery for unauthenticated users
        if ("GET".equalsIgnoreCase(request.getMethod())
                && ("/mentors".equals(path)
                || path.startsWith("/mentors/search")
                || path.matches("^/mentors/\\d+$"))) {
            chain.doFilter(request, response);
            return;
        }

        // Public skill discovery for unauthenticated users
        if ("GET".equalsIgnoreCase(request.getMethod())
                && ("/skills".equals(path)
                || path.startsWith("/skills/search")
                || path.matches("^/skills/\\d+$"))) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            sendError(response, "Invalid or expired JWT token");
            return;
        }

        // Role/self-based authorization for user-service APIs (Udemy/Coursera-like)
        if (path.startsWith("/users")) {
            String role = jwtUtil.extractRole(token);
            Long tokenUserId = jwtUtil.extractUserId(token);

            // Admin-only endpoints
            if ("/users".equals(path)) {
                if ("GET".equalsIgnoreCase(request.getMethod()) || "POST".equalsIgnoreCase(request.getMethod())) {
                    if (!"ROLE_ADMIN".equals(role)) {
                        sendError(response, "Admin access required");
                        return;
                    }
                }
            }
            if (path.startsWith("/users/email") || path.startsWith("/users/search")) {
                if (!"ROLE_ADMIN".equals(role)) {
                    sendError(response, "Admin access required");
                    return;
                }
            }

            // Self (or admin) for /users/{id} GET/PUT/DELETE
            Matcher m = USER_ID_PATH.matcher(path);
            if (m.matches()) {
                long pathUserId = Long.parseLong(m.group(1));
                boolean isAdmin = "ROLE_ADMIN".equals(role);
                boolean isSelf = tokenUserId != null && tokenUserId == pathUserId;

                if (!isAdmin && !isSelf) {
                    sendError(response, "Forbidden");
                    return;
                }
            }
        }

        // Role-based authorization for mentor-service APIs
        if (path.startsWith("/mentors")) {
            String role = jwtUtil.extractRole(token);
            Long tokenUserId = jwtUtil.extractUserId(token);
            String method = request.getMethod();

            // Learner applies to become mentor (admin can also trigger for operations)
            if ("/mentors/apply".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_LEARNER", "ROLE_ADMIN")) {
                    sendError(response, "Only learner or admin can apply as mentor");
                    return;
                }
            }

            // Admin-only mentor approval/rejection
            Matcher statusMatcher = MENTOR_STATUS_PATH.matcher(path);
            if (statusMatcher.matches() && "PATCH".equalsIgnoreCase(method)) {
                if (!"ROLE_ADMIN".equals(role)) {
                    sendError(response, "Admin access required");
                    return;
                }
            }

            // Mentor/admin can update availability
            Matcher availabilityMatcher = MENTOR_AVAILABILITY_PATH.matcher(path);
            if (availabilityMatcher.matches() && "PUT".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_MENTOR", "ROLE_ADMIN")) {
                    sendError(response, "Mentor or admin access required");
                    return;
                }
            }

            // /mentors/user/{id}: self or admin
            Matcher mentorUserMatcher = MENTOR_USER_PATH.matcher(path);
            if (mentorUserMatcher.matches() && "GET".equalsIgnoreCase(method)) {
                long pathUserId = Long.parseLong(mentorUserMatcher.group(1));
                boolean isAdmin = "ROLE_ADMIN".equals(role);
                boolean isSelf = tokenUserId != null && tokenUserId == pathUserId;
                if (!isAdmin && !isSelf) {
                    sendError(response, "Forbidden");
                    return;
                }
            }

            // Discovery endpoints are available to authenticated roles only
            if (("/mentors".equals(path) || path.startsWith("/mentors/search") || path.matches("^/mentors/\\d+$"))
                    && "GET".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_LEARNER", "ROLE_MENTOR", "ROLE_ADMIN")) {
                    sendError(response, "Access denied");
                    return;
                }
            }
        }

        // Role-based authorization for skill-service APIs
        if (path.startsWith("/skills")) {
            String role = jwtUtil.extractRole(token);
            String method = request.getMethod();
            boolean isWriteOperation =
                    "POST".equalsIgnoreCase(method)
                            || "PUT".equalsIgnoreCase(method)
                            || "DELETE".equalsIgnoreCase(method)
                            || "PATCH".equalsIgnoreCase(method);

            if (isWriteOperation && !"ROLE_ADMIN".equals(role)) {
                sendError(response, "Admin access required");
                return;
            }
        }

        // Role-based authorization for session-service APIs
        if (path.startsWith("/sessions")) {
            String role = jwtUtil.extractRole(token);
            String method = request.getMethod();

            if ("/sessions".equals(path) && "POST".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_LEARNER", "ROLE_ADMIN")) {
                    sendError(response, "Only learners or admins can book sessions");
                    return;
                }
            }

            if (path.matches("^/sessions/\\d+/(accept|reject|complete|cancel)$") && "POST".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_MENTOR", "ROLE_ADMIN")) {
                    sendError(response, "Only mentors or admins can manage session status");
                    return;
                }
            }
        }

        // Role-based authorization for review-service APIs
        if (path.startsWith("/reviews")) {
            String role = jwtUtil.extractRole(token);
            String method = request.getMethod();

            if ("POST".equalsIgnoreCase(method)) {
                if (!hasAnyRole(role, "ROLE_LEARNER", "ROLE_ADMIN")) {
                    sendError(response, "Only learners or admins can submit reviews");
                    return;
                }
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                if (!"ROLE_ADMIN".equals(role)) {
                    sendError(response, "Admin access required to delete reviews");
                    return;
                }
            }

            if ("GET".equalsIgnoreCase(method)) {
                // GET /reviews/mentor/{mentorId}
                if (path.matches("^/reviews/mentor/\\d+$")) {
                    if (!hasAnyRole(role, "ROLE_MENTOR", "ROLE_ADMIN", "ROLE_LEARNER")) {
                        sendError(response, "Access denied");
                        return;
                    }
                }

                // GET /reviews/learner/{learnerId}
                Matcher learnerMatcher = REVIEW_LEARNER_PATH.matcher(path);
                if (learnerMatcher.matches()) {
                    long pathLearnerId = Long.parseLong(learnerMatcher.group(1));
                    Long tokenUserId = jwtUtil.extractUserId(token);
                    boolean isAdmin = "ROLE_ADMIN".equals(role);
                    boolean isSelf = (tokenUserId != null && tokenUserId == pathLearnerId);

                    if (!isAdmin && !isSelf) {
                        sendError(response, "Forbidden");
                        return;
                    }
                }

                // GET /reviews/mentor/{mentorId}/average implicitly allowed for any authenticated user
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean hasAnyRole(String actualRole, String... allowedRoles) {
        if (actualRole == null) return false;
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(actualRole)) return true;
        }
        return false;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}