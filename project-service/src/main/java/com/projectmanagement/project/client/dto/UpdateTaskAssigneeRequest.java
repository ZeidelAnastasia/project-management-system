package com.projectmanagement.project.client.dto;

import java.util.UUID;

public record UpdateTaskAssigneeRequest(
        UUID assigneeId
) {
}