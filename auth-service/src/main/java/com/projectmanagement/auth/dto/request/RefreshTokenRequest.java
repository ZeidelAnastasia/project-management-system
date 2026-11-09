package com.projectmanagement.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для обновления или отзыва refresh-токена")
public record RefreshTokenRequest(

        @Schema(description = "Refresh-токен", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank
        String refreshToken
) {
}