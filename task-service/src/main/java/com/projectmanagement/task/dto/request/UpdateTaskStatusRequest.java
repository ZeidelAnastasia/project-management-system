
package com.projectmanagement.task.dto.request;

import com.projectmanagement.task.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for updating task status")
public record UpdateTaskStatusRequest(

        @NotNull
        @Schema(description = "New task status", example = "IN_PROGRESS")
        TaskStatus status
) {
}
