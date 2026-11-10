package com.projectmanagement.project.dto.response;

import com.projectmanagement.project.entity.enums.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Project member information")
public record ProjectMemberResponse(

        @Schema(description = "User ID")
        UUID userId,

        @Schema(description = "Member role within the project", example = "MEMBER")
        ProjectRole projectRole,

        @Schema(description = "Date when the user joined the project")
        LocalDateTime joinedAt

) {
}