
package com.projectmanagement.task.dto.response;

import com.projectmanagement.task.entity.enums.TaskChangeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Task change history")
public record TaskHistoryResponse(

        @Schema(description = "History record ID")
        UUID id,

        @Schema(description = "User who made the change")
        UUID changedById,

        @Schema(description = "Type of change")
        TaskChangeType changeType,

        @Schema(description = "Previous value")
        String oldValue,

        @Schema(description = "New value")
        String newValue,

        @Schema(description = "Change timestamp")
        Instant changedAt
) {
}
