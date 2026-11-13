package com.projectmanagement.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDeadlineReminderResponse(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        String title,
        LocalDateTime deadline
) {}