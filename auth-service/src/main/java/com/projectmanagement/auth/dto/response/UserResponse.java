package com.projectmanagement.auth.dto.response;

import com.projectmanagement.auth.entity.enums.RoleCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Информация о пользователе")
public record UserResponse(

        @Schema(description = "Идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Электронная почта пользователя", example = "user@example.com")
        String email,

        @Schema(description = "Признак активности пользователя", example = "true")
        boolean active,

        @Schema(description = "Системные роли пользователя", example = "[\"EMPLOYEE\"]")
        Set<RoleCode> roles,

        @Schema(description = "Дата регистрации пользователя")
        LocalDateTime createdAt
) {
}