package com.projectmanagement.auth.service;

import com.projectmanagement.auth.dto.request.RoleUpdateRequest;
import com.projectmanagement.auth.dto.request.UserStatusUpdateRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.entity.RefreshToken;
import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.User;
import com.projectmanagement.auth.entity.UserRole;
import com.projectmanagement.auth.entity.enums.RoleCode;
import com.projectmanagement.auth.exception.ResourceNotFoundException;
import com.projectmanagement.auth.mapper.AuthMapper;
import com.projectmanagement.auth.repository.RefreshTokenRepository;
import com.projectmanagement.auth.repository.RoleRepository;
import com.projectmanagement.auth.repository.UserRepository;
import com.projectmanagement.auth.repository.UserRoleRepository;
import com.projectmanagement.auth.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        void getAllUsers_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = new User();
            Page<User> userPage = new PageImpl<>(List.of(user));
            UserResponse userResponse = mock(UserResponse.class);

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(authMapper.toUserResponse(user)).thenReturn(userResponse);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).containsExactly(userResponse);
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        void getUserById_Success() {
            UUID id = UUID.randomUUID();
            User user = new User();
            UserResponse userResponse = mock(UserResponse.class);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(authMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserById(id);

            assertThat(result).isEqualTo(userResponse);
        }

        @Test
        void getUserById_NotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));
        }
    }

    @Nested
    @DisplayName("updateUserRole")
    class UpdateUserRoleTests {

        @Test
        void updateUserRole_Success() {
            UUID id = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest(RoleCode.ADMIN);

            User user = new User();
            user.setId(id);
            user.setUserRoles(new HashSet<>());

            Role role = new Role();
            role.setCode(RoleCode.ADMIN);

            UserResponse userResponse = mock(UserResponse.class);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(roleRepository.findByCode(request.role())).thenReturn(Optional.of(role));
            when(authMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateUserRole(id, request);

            assertThat(result).isEqualTo(userResponse);
            verify(userRoleRepository).deleteAllByIdUserId(id);
            verify(userRoleRepository).save(any(UserRole.class));
            assertThat(user.getUserRoles()).hasSize(1);
        }

        @Test
        void updateUserRole_UserNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest(RoleCode.ADMIN);

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(id, request));
            verify(roleRepository, never()).findByCode(any());
            verify(userRoleRepository, never()).deleteAllByIdUserId(any());
        }

        @Test
        void updateUserRole_RoleNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest(RoleCode.ADMIN);
            User user = new User();
            user.setId(id);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(roleRepository.findByCode(request.role())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(id, request));
            verify(userRoleRepository, never()).deleteAllByIdUserId(any());
            verify(userRoleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatusTests {

        @Test
        void updateUserStatus_SetActiveTrue_Success() {
            UUID id = UUID.randomUUID();
            UserStatusUpdateRequest request = new UserStatusUpdateRequest(true);
            User user = new User();
            user.setId(id);
            user.setActive(false);

            UserResponse userResponse = mock(UserResponse.class);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(authMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateUserStatus(id, request);

            assertThat(result).isEqualTo(userResponse);
            assertThat(user.isActive()).isTrue();
            verifyNoInteractions(refreshTokenRepository);
        }

        @Test
        void updateUserStatus_SetActiveFalse_RevokesTokens_Success() {
            UUID id = UUID.randomUUID();
            UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);
            User user = new User();
            user.setId(id);
            user.setActive(true);

            RefreshToken token1 = new RefreshToken();
            RefreshToken token2 = new RefreshToken();

            UserResponse userResponse = mock(UserResponse.class);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(id)).thenReturn(List.of(token1, token2));
            when(authMapper.toUserResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateUserStatus(id, request);

            assertThat(result).isEqualTo(userResponse);
            assertThat(user.isActive()).isFalse();
            assertThat(token1.getRevokedAt()).isNotNull();
            assertThat(token2.getRevokedAt()).isNotNull();
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        void updateUserStatus_UserNotFound_ThrowsException() {
            UUID id = UUID.randomUUID();
            UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.updateUserStatus(id, request));
            verifyNoInteractions(refreshTokenRepository);
        }
    }
}