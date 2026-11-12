
package com.projectmanagement.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

@Schema(description = "Request for changing task deadline")
public record UpdateTaskDeadlineRequest(

        @FutureOrPresent
        @Schema(description = "New task deadline", example = "2026-12-20")
        LocalDate dueDate
) {
}
