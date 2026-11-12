
package com.projectmanagement.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Detailed task information")
public record TaskDetailsResponse(

        @Schema(description = "Task information")
        TaskResponse task,

        @Schema(description = "Task creator ID")
        UUID createdById,

        @Schema(description = "Task change history")
        List<TaskHistoryResponse> history
) {
}
