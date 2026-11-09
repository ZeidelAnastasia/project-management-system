package com.projectmanagement.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для авторизации пользователя")
public record LoginRequest(

        @Schema(description = "Электронная почта пользователя", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Пароль пользователя", example = "Password123!")
        @NotBlank
        String password
) {
}