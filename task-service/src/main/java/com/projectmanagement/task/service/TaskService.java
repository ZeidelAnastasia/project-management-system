package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.CreateTaskRequest;
import com.projectmanagement.task.dto.request.TaskFilterRequest;
import com.projectmanagement.task.dto.request.UpdateTaskRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(UUID projectId, CreateTaskRequest request);

    TaskResponse getTask(UUID taskId);

    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request);

    void deleteTask(UUID taskId);

    Page<TaskResponse> getProjectTasks(
            UUID projectId,
            TaskFilterRequest filter
    );

    Page<TaskResponse> searchTasks(TaskFilterRequest filter);
}