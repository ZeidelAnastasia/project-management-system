package com.projectmanagement.reporting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Отчёт о динамике выполнения задач проекта")
public class CompletionDynamicsResponse {

    @Schema(description = "Идентификатор проекта", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Начало отчётного периода", example = "2026-11-01")
    private LocalDate startDate;

    @Schema(description = "Окончание отчётного периода", example = "2026-11-14")
    private LocalDate endDate;

    @Schema(description = "Показатели выполнения задач по дням")
    private List<CompletionDynamicsItem> dynamics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Показатели выполнения задач за отдельную дату")
    public static class CompletionDynamicsItem {

        @Schema(description = "Дата", example = "2026-11-14")
        private LocalDate date;

        @Schema(description = "Количество созданных задач", example = "5")
        private long createdTasks;

        @Schema(description = "Количество завершённых задач", example = "3")
        private long completedTasks;
    }
}