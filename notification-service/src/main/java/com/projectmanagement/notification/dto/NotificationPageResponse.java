package com.projectmanagement.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Страница уведомлений")
public record NotificationPageResponse(

        @Schema(description = "Список уведомлений")
        List<NotificationResponse> content,

        @Schema(description = "Номер текущей страницы", example = "0")
        int page,

        @Schema(description = "Размер страницы", example = "20")
        int size,

        @Schema(description = "Общее количество уведомлений", example = "45")
        long totalElements,

        @Schema(description = "Общее количество страниц", example = "3")
        int totalPages,

        @Schema(description = "Является ли страница первой")
        boolean first,

        @Schema(description = "Является ли страница последней")
        boolean last
) {
}