package com.projectmanagement.task.api;

import com.projectmanagement.task.dto.request.*;
import com.projectmanagement.task.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tasks", description = "Task management API")
@RequestMapping("/api/tasks")
public interface TaskApi {

    @Operation(summary = "Get task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{taskId}")
    ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task ID")
            @PathVariable("taskId") UUID taskId
    );

    @Operation(summary = "Update task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{taskId}")
    ResponseEntity<TaskResponse> updateTask(
            @PathVariable("taskId") UUID taskId,
            @RequestBody @jakarta.validation.Valid UpdateTaskRequest request
    );

    @Operation(summary = "Delete task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> deleteTask(
            @PathVariable("taskId") UUID taskId
    );

    @Operation(summary = "Update task status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task status updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    @PatchMapping("/{taskId}/status")
    ResponseEntity<TaskResponse> updateStatus(
            @PathVariable("taskId") UUID taskId,
            @RequestBody @jakarta.validation.Valid UpdateTaskStatusRequest request
    );

    @Operation(summary = "Update task assignee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task assignee updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task or assignee not found")
    })
    @PatchMapping("/{taskId}/assignee")
    ResponseEntity<TaskResponse> updateAssignee(
            @PathVariable("taskId") UUID taskId,
            @RequestBody @jakarta.validation.Valid UpdateTaskAssigneeRequest request
    );

    @Operation(summary = "Update task deadline")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deadline updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{taskId}/deadline")
    ResponseEntity<TaskResponse> updateDeadline(
            @PathVariable("taskId") UUID taskId,
            @RequestBody @jakarta.validation.Valid UpdateTaskDeadlineRequest request
    );

    @Operation(summary = "Update actual task effort")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task effort updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{taskId}/effort")
    ResponseEntity<TaskResponse> updateEffort(
            @PathVariable("taskId") UUID taskId,
            @RequestBody @jakarta.validation.Valid UpdateTaskEffortRequest request
    );

    @Operation(summary = "Get task change history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task history retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{taskId}/history")
    ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(
            @PathVariable("taskId") UUID taskId
    );

    @Operation(summary = "Search tasks using filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/search")
    ResponseEntity<Page<TaskResponse>> searchTasks(
            @ParameterObject @ModelAttribute TaskFilterRequest filter
    );
}