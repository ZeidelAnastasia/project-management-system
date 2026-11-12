package com.projectmanagement.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating a label")
public record UpdateLabelRequest(

        @Schema(
                description = "New label name",
                example = "Frontend",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(
                description = "New label color in HEX format",
                example = "#2ECC71",
                pattern = "^#[0-9A-Fa-f]{6}$",
                nullable = true
        )
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
        String color
) {
}