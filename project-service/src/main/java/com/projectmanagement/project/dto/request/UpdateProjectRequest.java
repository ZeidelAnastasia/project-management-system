package com.projectmanagement.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating a project")
public record UpdateProjectRequest(

        @Schema(
                description = "Updated project name",
                example = "Updated Project Name",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        String name,

        @Schema(
                description = "Updated project description",
                example = "Updated project description"
        )
        String description

) {
}