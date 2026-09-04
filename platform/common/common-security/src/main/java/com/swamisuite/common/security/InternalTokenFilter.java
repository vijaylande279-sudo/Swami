package com.swamisuite.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Guards /internal/** service-to-service endpoints with a static shared token
 * (config-server distributed). These paths are never routed through the gateway,
 * but this filter is the actual enforcement in case that routing rule is ever
 * misconfigured.
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    private final String expectedToken;

    public InternalTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) {
            String provided = request.getHeader("X-Internal-Token");
            if (expectedToken == null || expectedToken.isBlank() || !expectedToken.equals(provided)) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"Invalid internal token\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
