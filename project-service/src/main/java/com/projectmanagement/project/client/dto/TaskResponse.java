package com.projectmanagement.project.client.dto;

import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        UUID assigneeId,
        String title,
        String status
) {
}