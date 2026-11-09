package com.projectmanagement.auth.service;

import com.projectmanagement.auth.dto.request.LoginRequest;
import com.projectmanagement.auth.dto.request.RefreshTokenRequest;
import com.projectmanagement.auth.dto.request.RegisterRequest;
import com.projectmanagement.auth.dto.response.AuthResponse;
import com.projectmanagement.auth.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    UserResponse getCurrentUser();
}