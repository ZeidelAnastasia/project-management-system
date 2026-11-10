package com.projectmanagement.project.controller.api;

import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.entity.enums.ProjectRole;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Validated
@RequestMapping("/api/projects/{projectId}/members")
@Tag(name = "Project Members", description = "Project member management operations")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectMemberApi {

    @PostMapping
    @Operation(
            summary = "Add a project member",
            description = "Adds a user to a project. Available to project managers and administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Member successfully added",
                    content = @Content(
                            schema = @Schema(implementation = ProjectMemberResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "User is already a project member")
    })
    ResponseEntity<ProjectMemberResponse> addMember(
            @Parameter(
                    description = "Project ID",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable @NotNull UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    );

    @GetMapping
    @Operation(
            summary = "Get project members",
            description = "Returns the list of members of a project."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Members successfully retrieved"
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId
    );

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Remove a project member",
            description = "Removes a user from a project. Unfinished tasks assigned to this user must be reassigned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member successfully removed"),
            @ApiResponse(responseCode = "400", description = "Invalid reassignment data"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project or member not found"),
            @ApiResponse(responseCode = "409", description = "Member cannot be removed")
    })
    ResponseEntity<Void> removeMember(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId,
            @Parameter(description = "User ID of the member to remove", required = true)
            @PathVariable @NotNull UUID userId,
            @Parameter(
                    description = "User ID to whom unfinished tasks should be reassigned",
                    in = ParameterIn.QUERY
            )
            @RequestParam(required = false) UUID reassignToUserId
    );

    @PatchMapping("/{userId}/role")
    @Operation(
            summary = "Update project member role",
            description = "Changes the role of a project member. Available to project managers and administrators."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Member role successfully updated",
                    content = @Content(
                            schema = @Schema(implementation = ProjectMemberResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid project role"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project or member not found"),
            @ApiResponse(responseCode = "409", description = "Role cannot be changed")
    })
    ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @Parameter(description = "Project ID", required = true)
            @PathVariable @NotNull UUID projectId,
            @Parameter(description = "User ID of the member", required = true)
            @PathVariable @NotNull UUID userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New project role",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProjectRole.class)
                    )
            )
            @NotNull @RequestBody ProjectRole projectRole
    );
}