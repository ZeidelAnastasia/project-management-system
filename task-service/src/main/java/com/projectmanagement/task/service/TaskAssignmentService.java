package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.request.UpdateTaskAssigneeRequest;
import com.projectmanagement.task.dto.response.TaskResponse;

import java.util.UUID;

public interface TaskAssignmentService {

    TaskResponse updateAssignee(
            UUID taskId,
            UpdateTaskAssigneeRequest request
    );
}