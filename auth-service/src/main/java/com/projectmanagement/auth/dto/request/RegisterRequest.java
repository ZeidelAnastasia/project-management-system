package com.projectmanagement.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для регистрации пользователя")
public record RegisterRequest(

        @Schema(description = "Электронная почта пользователя", example = "user@example.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Пароль пользователя", example = "Password123!")
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}