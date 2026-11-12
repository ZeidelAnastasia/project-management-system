
package com.projectmanagement.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request for updating estimated task effort")
public record UpdateTaskEffortRequest(

        @NotNull
        @PositiveOrZero
        @Schema(description = "Estimated effort in hours", example = "12")
        Integer actualEffort
) {
}
