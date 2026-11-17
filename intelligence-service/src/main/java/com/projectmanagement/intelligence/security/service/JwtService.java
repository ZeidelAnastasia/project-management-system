package com.projectmanagement.intelligence.security.service;

import com.projectmanagement.intelligence.security.model.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public AuthenticatedUser validateAndExtract(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();

        if (userId == null || userId.isBlank()) {
            userId = claims.get("userId", String.class);
        }

        if (userId == null || userId.isBlank()) {
            throw new JwtException("JWT does not contain user identifier");
        }

        return new AuthenticatedUser(
                UUID.fromString(userId),
                extractRoles(claims.get("roles"))
        );
    }

    private Set<String> extractRoles(Object rolesClaim) {
        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                    .map(Object::toString)
                    .map(this::normalizeRole)
                    .collect(Collectors.toSet());
        }

        if (rolesClaim instanceof String roles) {
            return Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::normalizeRole)
                    .collect(Collectors.toSet());
        }

        return Set.of();
    }

    private String normalizeRole(String role) {
        return role.startsWith("ROLE_")
                ? role.substring(5)
                : role;
    }
}