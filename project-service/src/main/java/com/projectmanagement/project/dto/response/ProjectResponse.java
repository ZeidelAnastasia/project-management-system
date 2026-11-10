package com.projectmanagement.project.dto.response;

import com.projectmanagement.project.entity.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Project information")
public record ProjectResponse(

        @Schema(description = "Project ID")
        UUID id,

        @Schema(description = "Project name", example = "Project Management System")
        String name,

        @Schema(description = "Project description")
        String description,

        @Schema(description = "Current project status", example = "ACTIVE")
        ProjectStatus status,

        @Schema(description = "ID of the project creator")
        UUID createdBy,

        @Schema(description = "Project creation date")
        LocalDateTime createdAt,

        @Schema(description = "Date of the last project update")
        LocalDateTime updatedAt,

        @Schema(description = "Project archival date")
        LocalDateTime archivedAt,

        @Schema(description = "Project completion date")
        LocalDateTime completedAt

) {
}