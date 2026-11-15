package com.projectmanagement.reporting.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret
    ) {
        byte[] decodedSecret = Base64.getDecoder().decode(secret);

        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 256 bits"
            );
        }

        SecretKey key = new SecretKeySpec(
                decodedSecret,
                "HmacSHA256"
        );

        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}