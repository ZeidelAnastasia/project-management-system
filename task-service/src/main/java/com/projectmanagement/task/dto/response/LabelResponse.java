package com.projectmanagement.task.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Label information")
public record LabelResponse(

        @Schema(
                description = "Label identifier",
                example = "73a9291f-6cfd-46c9-9c28-e9c1b5970c79"
        )
        UUID id,

        @Schema(
                description = "Label name",
                example = "Backend"
        )
        String name,

        @Schema(
                description = "Label color in HEX format",
                example = "#3498DB",
                nullable = true
        )
        String color
) {
}