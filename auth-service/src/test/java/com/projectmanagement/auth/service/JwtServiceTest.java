package com.projectmanagement.auth.service;

import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.User;
import com.projectmanagement.auth.entity.UserRole;
import com.projectmanagement.auth.entity.enums.RoleCode;
import com.projectmanagement.auth.security.properties.JwtProperties;
import com.projectmanagement.auth.security.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @Test
    @DisplayName("Создание JWT с корректными subject, email, ролями и временем истечения")
    void generateAccessToken_ValidClaims_Success() {
        UUID userId = UUID.randomUUID();
        String email = "developer@example.com";
        long accessExpirationMillis = 3600000L;

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        Role role = new Role();
        role.setCode(RoleCode.EMPLOYEE);

        UserRole userRole = new UserRole(user, role);
        user.setUserRoles(Set.of(userRole));

        when(jwtProperties.getAccessExpiration()).thenReturn(accessExpirationMillis);

        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mocked.jwt.token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        when(jwtEncoder.encode(captor.capture())).thenReturn(mockJwt);

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isEqualTo("mocked.jwt.token");

        JwtEncoderParameters parameters = captor.getValue();
        assertThat(parameters.getJwsHeader().getAlgorithm()).isEqualTo(MacAlgorithm.HS256);

        JwtClaimsSet claims = parameters.getClaims();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.getClaimAsString("email")).isEqualTo(email);
        assertThat(claims.getClaimAsStringList("roles")).containsExactly(RoleCode.EMPLOYEE.name());

        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isNotNull();
        assertThat(claims.getExpiresAt())
                .isCloseTo(claims.getIssuedAt().plusMillis(accessExpirationMillis), within(1, ChronoUnit.SECONDS));

        verify(jwtEncoder).encode(parameters);
    }

    @Test
    @DisplayName("Проверка срока действия токена в секундах")
    void getAccessTokenExpiresInSeconds_ReturnsCorrectSeconds() {
        long accessExpirationMillis = 900000L;
        when(jwtProperties.getAccessExpiration()).thenReturn(accessExpirationMillis);

        long result = jwtService.getAccessTokenExpiresInSeconds();

        assertThat(result).isEqualTo(900L);
    }
}