package com.projectmanagement.task.event;

import java.util.UUID;

public record TaskEffortUpdatedPayload(
        UUID taskId,
        UUID projectId,
        UUID assigneeId,
        Integer oldEffort,
        Integer newEffort
) {
}