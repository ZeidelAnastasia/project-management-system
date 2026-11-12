package com.projectmanagement.task.event;

import java.time.LocalDate;
import java.util.UUID;

public record TaskDeadlineChangedPayload(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        LocalDate oldDeadline,
        LocalDate newDeadline
) {
}