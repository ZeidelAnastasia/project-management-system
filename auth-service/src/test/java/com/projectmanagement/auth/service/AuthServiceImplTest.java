package com.projectmanagement.auth.service;

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
import com.projectmanagement.auth.mapper.AuthMapper;
import com.projectmanagement.auth.repository.RefreshTokenRepository;
import com.projectmanagement.auth.repository.RoleRepository;
import com.projectmanagement.auth.repository.UserRepository;
import com.projectmanagement.auth.repository.UserRoleRepository;
import com.projectmanagement.auth.security.SecurityUtils;
import com.projectmanagement.auth.security.service.JwtService;
import com.projectmanagement.auth.security.service.RefreshTokenGenerator;
import com.projectmanagement.auth.security.service.RefreshTokenHasher;
import com.projectmanagement.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        void register_Success() {
            RegisterRequest request = new RegisterRequest("test@example.com", "Password123!");
            Role defaultRole = new Role();
            defaultRole.setCode(RoleCode.EMPLOYEE);

            when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
            when(roleRepository.findByCode(RoleCode.EMPLOYEE)).thenReturn(Optional.of(defaultRole));
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(UUID.randomUUID());
                u.setCreatedAt(LocalDateTime.now());
                return u;
            });

            UserResponse mockResponse = mock(UserResponse.class);
            when(authMapper.toUserResponse(any(User.class))).thenReturn(mockResponse);

            UserResponse result = authService.register(request);

            assertThat(result).isNotNull().isEqualTo(mockResponse);
            verify(userRepository).save(any(User.class));
            verify(userRoleRepository).save(any(UserRole.class));
            verify(applicationEventPublisher).publishEvent(any(UserRegisteredEvent.class));
        }

        @Test
        void register_EmailAlreadyExists_ThrowsException() {
            RegisterRequest request = new RegisterRequest("exist@example.com", "Password123!");
            when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        void register_DefaultRoleNotFound_ThrowsException() {
            RegisterRequest request = new RegisterRequest("test@example.com", "Password123!");
            when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
            when(roleRepository.findByCode(RoleCode.EMPLOYEE)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> authService.register(request));
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        void login_Success() {
            LoginRequest request = new LoginRequest("test@example.com", "Password123!");
            User user = new User();
            user.setEmail(request.email());
            user.setPasswordHash("encodedPassword");
            user.setActive(true);

            when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("access-token");
            when(refreshTokenGenerator.generate()).thenReturn("refresh-token");
            when(refreshTokenHasher.hash(anyString())).thenReturn("hashed-refresh-token");

            AuthResponse mockAuthResponse = mock(AuthResponse.class);
            when(authMapper.toAuthResponse(any(), any(), anyLong(), any())).thenReturn(mockAuthResponse);

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull().isEqualTo(mockAuthResponse);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void login_UserNotFound_ThrowsException() {
            LoginRequest request = new LoginRequest("notfound@example.com", "Password123!");
            when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        }

        @Test
        void login_AccountDisabled_ThrowsException() {
            LoginRequest request = new LoginRequest("test@example.com", "Password123!");
            User user = new User();
            user.setActive(false);

            when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));

            assertThrows(AccountDisabledException.class, () -> authService.login(request));
        }

        @Test
        void login_WrongPassword_ThrowsException() {
            LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");
            User user = new User();
            user.setPasswordHash("encodedPassword");
            user.setActive(true);

            when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        void refresh_Success() {
            RefreshTokenRequest request = new RefreshTokenRequest("raw-refresh-token");
            String tokenHash = "hashed-token";

            User user = new User();
            user.setActive(true);

            RefreshToken token = new RefreshToken();
            token.setTokenHash(tokenHash);
            token.setUser(user);
            token.setExpiresAt(LocalDateTime.now().plusDays(1));
            token.setRevokedAt(null);

            when(refreshTokenHasher.hash(request.refreshToken())).thenReturn(tokenHash);
            when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

            AuthResponse mockAuthResponse = mock(AuthResponse.class);
            when(authMapper.toAuthResponse(any(), any(), anyLong(), any())).thenReturn(mockAuthResponse);

            AuthResponse response = authService.refresh(request);

            assertThat(response).isNotNull();
            assertThat(token.getRevokedAt()).isNotNull();
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        void refresh_TokenNotFound_ThrowsException() {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
            when(refreshTokenHasher.hash(anyString())).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));
        }

        @Test
        void refresh_TokenAlreadyRevoked_ThrowsException() {
            RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
            RefreshToken token = new RefreshToken();
            token.setRevokedAt(LocalDateTime.now().minusHours(1));

            when(refreshTokenHasher.hash(anyString())).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));
        }

        @Test
        void refresh_TokenExpired_ThrowsException() {
            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
            RefreshToken token = new RefreshToken();
            token.setExpiresAt(LocalDateTime.now().minusMinutes(5));

            when(refreshTokenHasher.hash(anyString())).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));
        }
    }

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        void logout_Success() {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-token");
            RefreshToken token = new RefreshToken();
            token.setRevokedAt(null);

            when(refreshTokenHasher.hash(request.refreshToken())).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(token));

            authService.logout(request);

            assertThat(token.getRevokedAt()).isNotNull();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        void logout_AlreadyRevoked_DoesNotSaveAgain() {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-token");
            RefreshToken token = new RefreshToken();
            token.setRevokedAt(LocalDateTime.now().minusDays(1));

            when(refreshTokenHasher.hash(request.refreshToken())).thenReturn("hashed");
            when(refreshTokenRepository.findByTokenHash("hashed")).thenReturn(Optional.of(token));

            authService.logout(request);

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        void getCurrentUser_Success() {
            UUID userId = UUID.randomUUID();
            User user = new User();
            UserResponse mockResponse = mock(UserResponse.class);

            when(securityUtils.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(authMapper.toUserResponse(user)).thenReturn(mockResponse);

            UserResponse result = authService.getCurrentUser();

            assertThat(result).isEqualTo(mockResponse);
        }

        @Test
        void getCurrentUser_NotFound_ThrowsException() {
            UUID userId = UUID.randomUUID();
            when(securityUtils.getCurrentUserId()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> authService.getCurrentUser());
        }
    }
}