package com.projectmanagement.auth.service;

import com.projectmanagement.auth.dto.request.RoleUpdateRequest;
import com.projectmanagement.auth.dto.request.UserStatusUpdateRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse updateUserRole(UUID id, RoleUpdateRequest request);

    UserResponse updateUserStatus(UUID id, UserStatusUpdateRequest request);
}