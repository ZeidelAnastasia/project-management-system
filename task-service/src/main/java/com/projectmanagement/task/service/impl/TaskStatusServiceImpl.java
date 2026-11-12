package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.UpdateTaskStatusRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.enums.TaskChangeType;
import com.projectmanagement.task.entity.enums.TaskStatus;
import com.projectmanagement.task.exception.InvalidTaskStatusTransitionException;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.CurrentUserService;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskEventPublisher;
import com.projectmanagement.task.service.TaskHistoryService;
import com.projectmanagement.task.service.TaskStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TaskAuthorizationService taskAuthorizationService;
    private final TaskHistoryService taskHistoryService;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    @Transactional
    public TaskResponse updateStatus(
            UUID taskId,
            UpdateTaskStatusRequest request
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        taskAuthorizationService.checkCanChangeStatus(task);

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.status();

        validateStatusTransition(oldStatus, newStatus);

        if (oldStatus == newStatus) {
            return taskMapper.toResponse(task);
        }

        task.setStatus(newStatus);

        Task updatedTask = taskRepository.save(task);

        taskHistoryService.recordChange(
                taskId,
                currentUserService.getCurrentUserId(),
                TaskChangeType.STATUS,
                oldStatus.name(),
                newStatus.name()
        );

        taskEventPublisher.publishTaskStatusChanged(
                updatedTask,
                oldStatus,
                newStatus
        );

        return taskMapper.toResponse(updatedTask);
    }

    private void validateStatusTransition(
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        if (oldStatus == null || newStatus == null) {
            throw new InvalidTaskStatusTransitionException("Invalid task status");
        }

        boolean validTransition = switch (oldStatus) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TaskStatus.TODO
                    || newStatus == TaskStatus.DONE;
            case DONE -> newStatus == TaskStatus.IN_PROGRESS;
        };

        if (!validTransition) {
            throw new InvalidTaskStatusTransitionException("Invalid task status");
        }
    }
}