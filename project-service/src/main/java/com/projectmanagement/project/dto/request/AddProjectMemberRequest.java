package com.projectmanagement.project.dto.request;

import com.projectmanagement.project.entity.enums.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request for adding a member to a project")
public record AddProjectMemberRequest(

        @Schema(
                description = "ID of the user to add",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "User ID is required")
        UUID userId,

        @Schema(
                description = "Member role within the project",
                example = "MEMBER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Project role is required")
        ProjectRole projectRole

) {
}