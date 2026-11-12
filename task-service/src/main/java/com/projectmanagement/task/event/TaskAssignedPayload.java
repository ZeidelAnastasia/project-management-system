package com.projectmanagement.task.event;

import java.util.UUID;

public record TaskAssignedPayload(
        UUID taskId,
        UUID projectId,
        UUID oldAssigneeId,
        UUID newAssigneeId
) {
}