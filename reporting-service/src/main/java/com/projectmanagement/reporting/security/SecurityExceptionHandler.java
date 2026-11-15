package com.projectmanagement.reporting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements
        AuthenticationEntryPoint,
        AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        writeProblemDetail(
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        writeProblemDetail(
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this operation"
        );
    }

    private void writeProblemDetail(
            HttpServletResponse response,
            HttpStatus status,
            String detail
    ) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(status.getReasonPhrase());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}