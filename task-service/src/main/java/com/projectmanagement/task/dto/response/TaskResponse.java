
package com.projectmanagement.task.dto.response;

import com.projectmanagement.task.entity.enums.TaskPriority;
import com.projectmanagement.task.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Task information")
public record TaskResponse(

        @Schema(description = "Task ID")
        UUID id,

        @Schema(description = "Project ID")
        UUID projectId,

        @Schema(description = "Assignee ID")
        UUID assigneeId,

        @Schema(description = "Task title")
        String title,

        @Schema(description = "Task description")
        String description,

        @Schema(description = "Task status")
        TaskStatus status,

        @Schema(description = "Task priority")
        TaskPriority priority,

        @Schema(description = "Task deadline")
        LocalDate dueDate,

        @Schema(description = "Estimated effort in hours")
        Integer estimatedEffort,

        @Schema(description = "Actual effort in hours")
        Integer actualEffort,

        @Schema(description = "Task labels")
        Set<LabelResponse> labels,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
}
