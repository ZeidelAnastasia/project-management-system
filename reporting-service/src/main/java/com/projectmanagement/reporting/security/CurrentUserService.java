package com.projectmanagement.reporting.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Jwt jwt = getCurrentJwt();

        return UUID.fromString(jwt.getSubject());
    }

    public List<String> getCurrentUserRoles() {
        Jwt jwt = getCurrentJwt();

        return jwt.getClaimAsStringList("roles");
    }

    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    private Jwt getCurrentJwt() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException(
                    "Authenticated JWT principal is not available"
            );
        }

        return jwt;
    }
}