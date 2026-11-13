package com.projectmanagement.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество непрочитанных уведомлений")
public record UnreadCountResponse(

        @Schema(description = "Количество непрочитанных уведомлений", example = "5")
        long count
) {
}