package com.projectmanagement.project.controller.api;

import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Validated
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management operations")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectApi {

    @PostMapping
    @Operation(
            summary = "Create a project",
            description = "Creates a new project. Available to administrators and project managers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Project successfully created",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request
    );

    @GetMapping("/{projectId}")
    @Operation(
            summary = "Get a project",
            description = "Returns information about a project if the current user has access to it."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project found",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<ProjectResponse> getProject(
            @Parameter(
                    description = "Project ID",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable @NotNull UUID projectId
    );

    @GetMapping
    @Operation(
            summary = "Get projects",
            description = "Returns projects available to the current user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects successfully retrieved"
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    ResponseEntity<List<ProjectResponse>> getProjects();

    @PutMapping("/{projectId}")
    @Operation(
            summary = "Update a project",
            description = "Updates project information. Available to the project manager and administrator."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project successfully updated",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project cannot be updated in its current state")
    })
    ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request
    );

    @PatchMapping("/{projectId}/archive")
    @Operation(
            summary = "Archive a project",
            description = "Archives an active project."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project successfully archived",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project cannot be archived in its current state")
    })
    ResponseEntity<ProjectResponse> archiveProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId
    );

    @PatchMapping("/{projectId}/restore")
    @Operation(
            summary = "Restore a project",
            description = "Restores an archived project."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project successfully restored",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project cannot be restored in its current state")
    })
    ResponseEntity<ProjectResponse> restoreProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId
    );

    @PatchMapping("/{projectId}/complete")
    @Operation(
            summary = "Complete a project",
            description = "Completes a project. If unfinished tasks remain, only an administrator can complete it with explicit confirmation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project successfully completed",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project cannot be completed in its current state")
    })
    ResponseEntity<ProjectResponse> completeProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId,
            @Valid @RequestBody CompleteProjectRequest request
    );
}