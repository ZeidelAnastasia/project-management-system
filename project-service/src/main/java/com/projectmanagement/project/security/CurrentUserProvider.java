package com.projectmanagement.project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException("JWT authentication is required");
        }

        Jwt jwt = jwtAuthentication.getToken();

        String userId = jwt.getSubject();

        if (userId == null || userId.isBlank()) {
            userId = jwt.getClaimAsString("userId");
        }

        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("User ID is missing in JWT");
        }

        String role = jwtAuthentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.equals(SecurityConstants.ROLE_ADMIN)
                        || authority.equals(SecurityConstants.ROLE_PROJECT_MANAGER)
                        || authority.equals(SecurityConstants.ROLE_EMPLOYEE))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("User role is missing in JWT")
                );

        return new CurrentUser(UUID.fromString(userId), role);
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().id();
    }

    public String getCurrentUserRole() {
        return getCurrentUser().role();
    }
}