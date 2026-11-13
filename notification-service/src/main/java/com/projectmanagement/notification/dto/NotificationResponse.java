package com.projectmanagement.notification.dto;

import com.projectmanagement.notification.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Уведомление пользователя")
public record NotificationResponse(

        @Schema(description = "ID уведомления")
        UUID id,

        @Schema(description = "Тип уведомления", example = "TASK_ASSIGNED")
        NotificationType type,

        @Schema(description = "Заголовок уведомления", example = "Вам назначена задача")
        String title,

        @Schema(description = "Текст уведомления")
        String message,

        @Schema(description = "ID связанной задачи")
        UUID taskId,

        @Schema(description = "ID связанного проекта")
        UUID projectId,

        @Schema(description = "Дата и время создания")
        LocalDateTime createdAt,

        @Schema(description = "Дата и время прочтения; null, если уведомление не прочитано")
        LocalDateTime readAt
) {
}