
package com.projectmanagement.task.dto.request;

import com.projectmanagement.task.entity.enums.TaskPriority;
import com.projectmanagement.task.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Task filtering and pagination parameters")
public record TaskFilterRequest(

        @Schema(description = "Filter by task status")
        TaskStatus status,

        @Schema(description = "Filter by task priority")
        TaskPriority priority,

        @Schema(description = "Filter by assignee ID")
        UUID assigneeId,

        @Schema(description = "Filter by deadline from")
        LocalDate dueDateFrom,

        @Schema(description = "Filter by deadline to")
        LocalDate dueDateTo,

        @Schema(description = "Filter by label ID")
        UUID labelId,

        @Schema(description = "Field for sorting", example = "dueDate")
        String sortBy,

        @Schema(description = "Sort direction", example = "ASC")
        String sortDirection,

        @Min(0)
        @Schema(description = "Page number", example = "0", defaultValue = "0")
        Integer page,

        @Min(1)
        @Max(100)
        @Schema(description = "Page size", example = "20", defaultValue = "20")
        Integer size
) {
}
