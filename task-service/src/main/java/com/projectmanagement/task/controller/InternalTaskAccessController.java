package com.projectmanagement.task.controller;

import com.projectmanagement.task.api.InternalTaskAccessApi;
import com.projectmanagement.task.client.TaskAccessService;
import com.projectmanagement.task.client.dto.TaskAccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalTaskAccessController implements InternalTaskAccessApi {

    private final TaskAccessService taskAccessService;

    @Override
    public TaskAccessResponse getTaskAccess(UUID taskId, UUID userId) {
        return taskAccessService.getTaskAccess(taskId, userId);
    }
}