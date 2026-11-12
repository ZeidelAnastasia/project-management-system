package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.UpdateTaskStatusRequest;
import com.projectmanagement.task.dto.response.TaskResponse;

import java.util.UUID;

public interface TaskStatusService {

    TaskResponse updateStatus(
            UUID taskId,
            UpdateTaskStatusRequest request
    );
}