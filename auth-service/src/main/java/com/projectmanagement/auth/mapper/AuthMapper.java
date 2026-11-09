package com.projectmanagement.auth.mapper;

import com.projectmanagement.auth.dto.response.AuthResponse;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.isActive(),
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getCode())
                        .collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }

    public AuthResponse toAuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            User user
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                toUserResponse(user)
        );
    }
}