package com.projectmanagement.task.event;

import java.util.UUID;

public record TaskDeletedPayload(
        UUID taskId,
        UUID projectId,
        UUID assigneeId
) {
}