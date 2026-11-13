package com.projectmanagement.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Задача для напоминания о приближающемся сроке")
public record DeadlineReminderTaskResponse(

        @Schema(description = "Идентификатор задачи")
        UUID taskId,

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Идентификатор исполнителя")
        UUID assigneeId,

        @Schema(description = "Название задачи")
        String title,

        @Schema(description = "Срок выполнения")
        LocalDate dueDate
) {
}