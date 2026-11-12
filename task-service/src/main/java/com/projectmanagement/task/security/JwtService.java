
package com.projectmanagement.task.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());

        Object roleClaim = claims.get("roles");

        if (roleClaim == null) {
            roleClaim = claims.get("role");
        }

        List<String> roles;

        if (roleClaim instanceof Collection<?> collection) {
            roles = collection.stream()
                    .map(String::valueOf)
                    .toList();
        } else if (roleClaim != null) {
            roles = List.of(String.valueOf(roleClaim));
        } else {
            throw new IllegalArgumentException("JWT does not contain user roles");
        }

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("JWT contains an empty roles list");
        }

        return new JwtUser(userId, roles);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
