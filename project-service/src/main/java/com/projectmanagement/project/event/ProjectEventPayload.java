package com.projectmanagement.project.event;

import java.time.Instant;
import java.util.UUID;

public record ProjectEventPayload(
        UUID projectId,
        String name,
        String description,
        String status,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt,
        Instant completedAt
) {
}