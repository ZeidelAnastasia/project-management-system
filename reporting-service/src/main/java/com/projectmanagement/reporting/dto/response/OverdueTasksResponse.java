package com.projectmanagement.reporting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Отчёт о просроченных задачах проекта")
public class OverdueTasksResponse {

    @Schema(description = "Идентификатор проекта", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Дата, на которую рассчитывается количество просроченных задач", example = "2026-11-14")
    private LocalDate date;

    @Schema(description = "Количество просроченных задач", example = "7")
    private long overdueTasks;
}