package com.projectmanagement.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат отметки уведомлений как прочитанных")
public record MarkAllAsReadResponse(

        @Schema(description = "Количество уведомлений, отмеченных как прочитанные", example = "5")
        int updatedCount
) {
}