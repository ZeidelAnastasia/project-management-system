package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.UpdateTaskDeadlineRequest;
import com.projectmanagement.task.dto.response.TaskResponse;

import java.util.UUID;

public interface TaskDeadlineService {

    TaskResponse updateDeadline(
            UUID taskId,
            UpdateTaskDeadlineRequest request
    );
}