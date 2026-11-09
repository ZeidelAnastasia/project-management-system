package com.projectmanagement.auth.controller;

import com.projectmanagement.auth.api.UserApi;
import com.projectmanagement.auth.dto.request.RoleUpdateRequest;
import com.projectmanagement.auth.dto.request.UserStatusUpdateRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    public ResponseEntity<UserResponse> getUserById(UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    public ResponseEntity<UserResponse> updateUserRole(
            UUID id,
            RoleUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    public ResponseEntity<UserResponse> updateUserStatus(
            UUID id,
            UserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request));
    }
}