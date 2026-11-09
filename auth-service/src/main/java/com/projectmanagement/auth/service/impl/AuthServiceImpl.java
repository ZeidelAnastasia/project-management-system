package com.projectmanagement.auth.service.impl;

import com.projectmanagement.auth.dto.request.LoginRequest;
import com.projectmanagement.auth.dto.request.RefreshTokenRequest;
import com.projectmanagement.auth.dto.request.RegisterRequest;
import com.projectmanagement.auth.dto.response.AuthResponse;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.entity.RefreshToken;
import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.User;
import com.projectmanagement.auth.entity.UserRole;
import com.projectmanagement.auth.entity.enums.RoleCode;
import com.projectmanagement.auth.event.UserRegisteredEvent;
import com.projectmanagement.auth.exception.*;
import com.projectmanagement.auth.kafka.event.UserRegisteredPayload;
import com.projectmanagement.auth.mapper.AuthMapper;
import com.projectmanagement.auth.repository.RefreshTokenRepository;
import com.projectmanagement.auth.repository.RoleRepository;
import com.projectmanagement.auth.repository.UserRepository;
import com.projectmanagement.auth.repository.UserRoleRepository;
import com.projectmanagement.auth.security.SecurityUtils;
import com.projectmanagement.auth.security.service.JwtService;
import com.projectmanagement.auth.security.service.RefreshTokenGenerator;
import com.projectmanagement.auth.security.service.RefreshTokenHasher;
import com.projectmanagement.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtService jwtService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final RefreshTokenHasher refreshTokenHasher;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        Role role = roleRepository.findByCode(RoleCode.EMPLOYEE)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);

        user = userRepository.save(user);

        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        user.setUserRoles(Set.of(userRole));

        applicationEventPublisher.publishEvent(
                new UserRegisteredEvent(
                        new UserRegisteredPayload(
                                user.getId(),
                                user.getEmail(),
                                user.getCreatedAt().toInstant(ZoneOffset.UTC)
                        )
                )
        );

        return authMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AccountDisabledException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return createAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = refreshTokenHasher.hash(request.refreshToken());

        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (currentToken.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (!currentToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        User user = currentToken.getUser();

        if (!user.isActive()) {
            throw new AccountDisabledException("User account is disabled");
        }

        currentToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(currentToken);

        return createAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = refreshTokenHasher.hash(request.refreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(token);
                    }
                });
    }

    @Override
    public UserResponse getCurrentUser() {
        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return authMapper.toUserResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenGenerator.generate();

        saveRefreshToken(user, refreshToken);

        return authMapper.toAuthResponse(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiresInSeconds(),
                user
        );
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refreshTokenHasher.hash(token));
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusNanos(refreshExpiration * 1_000_000)
        );

        refreshTokenRepository.save(refreshToken);
    }
}