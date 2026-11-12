
package com.projectmanagement.task.dto.request;

import com.projectmanagement.task.entity.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request for updating task information")
public record UpdateTaskRequest(

        @NotBlank
        @Size(max = 200)
        @Schema(description = "Task title", example = "Implement authentication")
        String title,

        @Size(max = 5000)
        @Schema(description = "Task description")
        String description,

        @NotNull
        @Schema(description = "Task priority", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "IDs of task labels")
        Set<UUID> labelIds
) {
}
