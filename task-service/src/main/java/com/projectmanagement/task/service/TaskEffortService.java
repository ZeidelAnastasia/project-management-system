package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.UpdateTaskEffortRequest;
import com.projectmanagement.task.dto.response.TaskResponse;

import java.util.UUID;

public interface TaskEffortService {

    TaskResponse updateEffort(
            UUID taskId,
            UpdateTaskEffortRequest request
    );
}