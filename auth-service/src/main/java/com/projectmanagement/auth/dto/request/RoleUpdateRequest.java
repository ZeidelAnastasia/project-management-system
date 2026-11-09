package com.projectmanagement.auth.dto.request;

import com.projectmanagement.auth.entity.enums.RoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные для изменения системной роли пользователя")
public record RoleUpdateRequest(

        @Schema(description = "Новая системная роль пользователя", example = "PROJECT_MANAGER")
        @NotNull
        RoleCode role
) {
}