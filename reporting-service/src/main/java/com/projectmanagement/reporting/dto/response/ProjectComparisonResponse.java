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
@Schema(description = "Отчёт для сравнения проектов за период")
public class ProjectComparisonResponse {

    @Schema(description = "Начало отчётного периода", example = "2026-11-01")
    private LocalDate startDate;

    @Schema(description = "Окончание отчётного периода", example = "2026-11-14")
    private LocalDate endDate;

    @Schema(description = "Сравнительные показатели проектов")
    private List<ProjectComparisonItem> projects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Аналитические показатели отдельного проекта")
    public static class ProjectComparisonItem {

        @Schema(description = "Идентификатор проекта", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID projectId;

        @Schema(description = "Количество созданных задач", example = "30")
        private long createdTasks;

        @Schema(description = "Количество завершённых задач", example = "22")
        private long completedTasks;

        @Schema(description = "Количество просроченных задач", example = "4")
        private long overdueTasks;
    }
}