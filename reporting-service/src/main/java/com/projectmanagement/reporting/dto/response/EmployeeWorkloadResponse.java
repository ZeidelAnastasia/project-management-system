package com.projectmanagement.reporting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Отчёт о загрузке сотрудников")
public class EmployeeWorkloadResponse {

    @Schema(description = "Идентификатор проекта", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Показатели загрузки сотрудников")
    private List<EmployeeWorkloadItem> employees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Показатели загрузки отдельного сотрудника")
    public static class EmployeeWorkloadItem {

        @Schema(description = "Идентификатор сотрудника", example = "650e8400-e29b-41d4-a716-446655440000")
        private UUID employeeId;

        @Schema(description = "Количество назначенных задач", example = "10")
        private long assignedTasks;

        @Schema(description = "Количество завершённых задач", example = "6")
        private long completedTasks;

        @Schema(description = "Количество просроченных задач", example = "2")
        private long overdueTasks;
    }
}