package com.projectmanagement.task.service;

import com.projectmanagement.task.dto.response.TaskHistoryResponse;
import com.projectmanagement.task.entity.enums.TaskChangeType;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryService {

    List<TaskHistoryResponse> getTaskHistory(UUID taskId);

    void recordChange(
            UUID taskId,
            UUID changedBy,
            TaskChangeType changeType,
            String oldValue,
            String newValue
    );
}