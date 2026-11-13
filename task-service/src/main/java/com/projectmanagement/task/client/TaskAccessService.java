package com.projectmanagement.task.client;

import com.projectmanagement.task.client.dto.TaskAccessResponse;

import java.util.UUID;

public interface TaskAccessService {

    TaskAccessResponse getTaskAccess(UUID taskId, UUID userId);
}
