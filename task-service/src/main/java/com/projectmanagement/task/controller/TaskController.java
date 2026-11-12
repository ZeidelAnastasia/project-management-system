package com.projectmanagement.task.controller;

import com.projectmanagement.task.api.TaskApi;
import com.projectmanagement.task.dto.request.*;
import com.projectmanagement.task.dto.response.*;
import com.projectmanagement.task.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController implements TaskApi {

    private final TaskService taskService;
    private final TaskStatusService taskStatusService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskDeadlineService taskDeadlineService;
    private final TaskEffortService taskEffortService;
    private final TaskHistoryService taskHistoryService;

    @Override
    public ResponseEntity<TaskResponse> getTask(UUID taskId) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

    @Override
    public ResponseEntity<TaskResponse> updateTask(
            UUID taskId,
            UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @Override
    public ResponseEntity<Void> deleteTask(UUID taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TaskResponse> updateStatus(
            UUID taskId,
            UpdateTaskStatusRequest request
    ) {
        return ResponseEntity.ok(taskStatusService.updateStatus(taskId, request));
    }

    @Override
    public ResponseEntity<TaskResponse> updateAssignee(
            UUID taskId,
            UpdateTaskAssigneeRequest request
    ) {
        return ResponseEntity.ok(taskAssignmentService.updateAssignee(taskId, request));
    }

    @Override
    public ResponseEntity<TaskResponse> updateDeadline(
            UUID taskId,
            UpdateTaskDeadlineRequest request
    ) {
        return ResponseEntity.ok(taskDeadlineService.updateDeadline(taskId, request));
    }

    @Override
    public ResponseEntity<TaskResponse> updateEffort(
            UUID taskId,
            UpdateTaskEffortRequest request
    ) {
        return ResponseEntity.ok(taskEffortService.updateEffort(taskId, request));
    }

    @Override
    public ResponseEntity<List<TaskHistoryResponse>> getTaskHistory(UUID taskId) {
        return ResponseEntity.ok(taskHistoryService.getTaskHistory(taskId));
    }

    @Override
    public ResponseEntity<Page<TaskResponse>> searchTasks(TaskFilterRequest filter) {
        return ResponseEntity.ok(taskService.searchTasks(filter));
    }
}