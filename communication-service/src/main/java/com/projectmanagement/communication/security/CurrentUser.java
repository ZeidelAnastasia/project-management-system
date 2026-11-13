package com.projectmanagement.communication.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CurrentUser {

    public UUID getUserId() {
        Authentication authentication = getAuthentication();
        return UUID.fromString(authentication.getName());
    }

    public boolean hasRole(String role) {
        return getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public String getAccessToken() {
        Object details = getAuthentication().getDetails();

        if (details instanceof String token) {
            return token;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Access token is missing"
        );
    }

    private Authentication getAuthentication() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        return authentication;
    }
}