package com.projectmanagement.auth.service.impl;

import com.projectmanagement.auth.dto.request.RoleUpdateRequest;
import com.projectmanagement.auth.dto.request.UserStatusUpdateRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.User;
import com.projectmanagement.auth.entity.UserRole;
import com.projectmanagement.auth.exception.ResourceNotFoundException;
import com.projectmanagement.auth.mapper.AuthMapper;
import com.projectmanagement.auth.repository.RefreshTokenRepository;
import com.projectmanagement.auth.repository.RoleRepository;
import com.projectmanagement.auth.repository.UserRepository;
import com.projectmanagement.auth.repository.UserRoleRepository;
import com.projectmanagement.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthMapper authMapper;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(authMapper::toUserResponse);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = findUserById(id);
        return authMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(UUID id, RoleUpdateRequest request) {
        User user = findUserById(id);

        Role role = roleRepository.findByCode(request.role())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + request.role()
                ));

        user.getUserRoles().clear();
        userRoleRepository.deleteAllByIdUserId(id);

        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        user.setUserRoles(Set.of(userRole));

        return authMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID id, UserStatusUpdateRequest request) {
        User user = findUserById(id);

        user.setActive(request.active());

        if (!request.active()) {
            revokeUserRefreshTokens(user);
        }

        return authMapper.toUserResponse(user);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id
                ));
    }

    private void revokeUserRefreshTokens(User user) {
        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository
                .findAllByUserIdAndRevokedAtIsNull(user.getId())
                .forEach(token -> {
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                });
    }
}