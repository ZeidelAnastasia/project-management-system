
package com.projectmanagement.task.dto.request;

import com.projectmanagement.task.entity.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Request for creating a task")
public record CreateTaskRequest(

        @NotBlank
        @Size(max = 200)
        @Schema(description = "Task title", example = "Implement authentication")
        String title,

        @Size(max = 5000)
        @Schema(description = "Task description", example = "Implement JWT authentication")
        String description,

        @Schema(description = "Assigned employee ID")
        UUID assigneeId,

        @NotNull
        @Schema(description = "Task priority", example = "HIGH")
        TaskPriority priority,

        @FutureOrPresent
        @Schema(description = "Task deadline", example = "2026-12-15")
        LocalDate dueDate,

        @PositiveOrZero
        @Schema(description = "Estimated effort in hours", example = "16")
        Integer estimatedEffort,

        @Schema(description = "IDs of task labels")
        Set<UUID> labelIds
) {
}
