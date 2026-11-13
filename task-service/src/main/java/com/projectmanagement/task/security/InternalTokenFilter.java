package com.projectmanagement.task.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH = "/api/internal/";
    private static final String TOKEN_HEADER = "X-Internal-Token";

    private final InternalTokenProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String expectedToken = properties.getToken();
        String actualToken = request.getHeader(TOKEN_HEADER);

        if (expectedToken == null || expectedToken.isBlank()
                || actualToken == null
                || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                actualToken.getBytes(StandardCharsets.UTF_8)
        )) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid internal token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}