
package com.projectmanagement.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request for changing task assignee")
public record UpdateTaskAssigneeRequest(

        @NotNull
        @Schema(description = "New assignee ID")
        UUID assigneeId
) {
}
