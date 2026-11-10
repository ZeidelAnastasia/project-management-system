package com.projectmanagement.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating a project")
public record CreateProjectRequest(

        @Schema(
                description = "Project name",
                example = "Project Management System",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        String name,

        @Schema(
                description = "Project description",
                example = "System for managing IT projects and tasks"
        )
        String description

) {
}