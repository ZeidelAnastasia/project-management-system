package com.projectmanagement.reporting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Общие показатели аналитической отчётности")
public class ReportSummaryResponse {

    @Schema(description = "Количество созданных задач", example = "120")
    private long createdTasks;

    @Schema(description = "Количество завершённых задач", example = "85")
    private long completedTasks;

    @Schema(description = "Количество просроченных задач", example = "12")
    private long overdueTasks;

    @Schema(description = "Количество созданных проектов", example = "8")
    private long createdProjects;

    @Schema(description = "Количество завершённых проектов", example = "3")
    private long completedProjects;
}