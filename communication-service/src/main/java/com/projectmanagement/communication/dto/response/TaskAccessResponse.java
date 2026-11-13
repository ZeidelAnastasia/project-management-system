package com.projectmanagement.communication.dto.response;

import java.util.UUID;

public record TaskAccessResponse(
        boolean taskExists,
        UUID projectId,
        UUID assigneeId,
        boolean projectMember,
        boolean projectManager
) {
}