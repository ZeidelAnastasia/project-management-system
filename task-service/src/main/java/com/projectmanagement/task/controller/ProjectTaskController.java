package com.projectmanagement.task.controller;

import com.projectmanagement.task.api.ProjectTaskApi;
import com.projectmanagement.task.dto.request.CreateTaskRequest;
import com.projectmanagement.task.dto.request.TaskFilterRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectTaskController implements ProjectTaskApi {

    private final TaskService taskService;

    @Override
    public ResponseEntity<TaskResponse> createTask(
            UUID projectId,
            CreateTaskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, request));
    }

    @Override
    public ResponseEntity<Page<TaskResponse>> getProjectTasks(
            UUID projectId,
            TaskFilterRequest filter
    ) {
        return ResponseEntity.ok(taskService.getProjectTasks(projectId, filter));
    }
}