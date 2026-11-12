package com.projectmanagement.project.controller;

import com.projectmanagement.project.dto.response.ProjectAccessResponse;
import com.projectmanagement.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class InternalProjectController {

    private final ProjectMemberService projectMemberService;

    @Operation(
            summary = "Get project access information",
            description = "Returns project existence, membership and project manager status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access information retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectId}/access")
    public ResponseEntity<ProjectAccessResponse> getProjectAccess(
            @Parameter(description = "Project identifier")
            @PathVariable UUID projectId,
            @Parameter(description = "User identifier")
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(
                projectMemberService.getProjectAccess(projectId, userId)
        );
    }
}