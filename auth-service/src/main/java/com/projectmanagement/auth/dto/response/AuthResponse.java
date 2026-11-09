package com.projectmanagement.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат успешной аутентификации")
public record AuthResponse(

        @Schema(description = "JWT access-токен", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Refresh-токен", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "Тип токена", example = "Bearer")
        String tokenType,

        @Schema(description = "Время жизни access-токена в секундах", example = "900")
        long expiresIn,

        @Schema(description = "Информация об аутентифицированном пользователе")
        UserResponse user
) {
}