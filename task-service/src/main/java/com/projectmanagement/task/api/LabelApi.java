package com.projectmanagement.task.api;

import com.projectmanagement.task.dto.request.CreateLabelRequest;
import com.projectmanagement.task.dto.request.UpdateLabelRequest;
import com.projectmanagement.task.dto.response.LabelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Labels",
        description = "API for managing task labels"
)
@RequestMapping("/api/labels")
public interface LabelApi {

    @Operation(
            summary = "Create label",
            description = "Creates a new label available for tasks across all projects"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Label successfully created",
                    content = @Content(
                            schema = @Schema(implementation = LabelResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LabelResponse createLabel(
            @Valid @RequestBody CreateLabelRequest request
    );

    @Operation(
            summary = "Get all labels",
            description = "Returns all available labels"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Labels successfully retrieved",
                    content = @Content(
                            schema = @Schema(
                                    implementation = LabelResponse.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    List<LabelResponse> getAllLabels();

    @Operation(
            summary = "Update label",
            description = "Updates the name and color of an existing label"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Label successfully updated",
                    content = @Content(
                            schema = @Schema(implementation = LabelResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @PutMapping("/{labelId}")
    LabelResponse updateLabel(
            @Parameter(
                    description = "Label identifier",
                    required = true,
                    example = "73a9291f-6cfd-46c9-9c28-e9c1b5970c79"
            )
            @PathVariable UUID labelId,

            @Valid @RequestBody UpdateLabelRequest request
    );

    @Operation(
            summary = "Delete label",
            description = "Deletes a label that is not assigned to any task"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Label successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Label not found"),
            @ApiResponse(responseCode = "409", description = "Label is assigned to one or more tasks")
    })
    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLabel(
            @Parameter(
                    description = "Label identifier",
                    required = true,
                    example = "73a9291f-6cfd-46c9-9c28-e9c1b5970c79"
            )
            @PathVariable UUID labelId
    );
}