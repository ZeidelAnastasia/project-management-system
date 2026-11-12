package com.projectmanagement.task.api;

import com.projectmanagement.task.dto.request.CreateTaskRequest;
import com.projectmanagement.task.dto.request.TaskFilterRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@Tag(name = "Project tasks", description = "Project task management API")
@RequestMapping("/api/projects/{projectId}/tasks")
public interface ProjectTaskApi {

    @Operation(summary = "Create task in project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project or assignee not found"),
            @ApiResponse(responseCode = "422", description = "Assignee is not a project member")
    })
    @PostMapping
    ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "Project ID")
            @PathVariable("projectId") UUID projectId,
            @RequestBody @Valid CreateTaskRequest request
    );

    @Operation(summary = "Get project tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping
    ResponseEntity<Page<TaskResponse>> getProjectTasks(
            @Parameter(description = "Project ID")
            @PathVariable("projectId") UUID projectId,
            @ParameterObject @ModelAttribute TaskFilterRequest filter
    );
}