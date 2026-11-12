package com.projectmanagement.task.client.dto;

public record ProjectAccessResponse(
        boolean projectExists,
        boolean projectMember,
        boolean projectManager
) {
}