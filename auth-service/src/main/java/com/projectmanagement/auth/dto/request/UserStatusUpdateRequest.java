package com.projectmanagement.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные для изменения статуса пользователя")
public record UserStatusUpdateRequest(

        @Schema(description = "Признак активности пользователя", example = "true")
        @NotNull
        Boolean active
) {
}