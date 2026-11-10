package com.projectmanagement.project.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final List<String> ROLE_CLAIMS = List.of(
            "roles",
            "role",
            "authorities"
    );

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return ROLE_CLAIMS.stream()
                .map(jwt::getClaim)
                .flatMap(this::extractRoles)
                .map(this::normalizeRole)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private Stream<String> extractRoles(Object value) {
        if (value instanceof String role) {
            return Stream.of(role);
        }

        if (value instanceof Collection<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast);
        }

        return Stream.empty();
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase();

        return normalized.startsWith("ROLE_")
                ? normalized
                : "ROLE_" + normalized;
    }
}