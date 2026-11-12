package com.projectmanagement.project.dto.response;

public record ProjectAccessResponse(
        boolean projectExists,
        boolean projectMember,
        boolean projectManager
) {
}