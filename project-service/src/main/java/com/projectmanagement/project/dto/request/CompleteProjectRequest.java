package com.projectmanagement.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

@Schema(description = "Request for completing a project")
public record CompleteProjectRequest(

        @Schema(
                description = "Explicit confirmation of project completion",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean confirmed

) {
}