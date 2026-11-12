package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.UpdateTaskDeadlineRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.enums.TaskChangeType;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.CurrentUserService;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskDeadlineService;
import com.projectmanagement.task.service.TaskEventPublisher;
import com.projectmanagement.task.service.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskDeadlineServiceImpl implements TaskDeadlineService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TaskAuthorizationService taskAuthorizationService;
    private final TaskHistoryService taskHistoryService;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    @Transactional
    public TaskResponse updateDeadline(UUID taskId, UpdateTaskDeadlineRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        taskAuthorizationService.checkCanManageTask(task);

        LocalDate oldDeadline = task.getDueDate();
        LocalDate newDeadline = request.dueDate();

        if (Objects.equals(oldDeadline, newDeadline)) {
            return taskMapper.toResponse(task);
        }

        task.setDueDate(newDeadline);
        Task updatedTask = taskRepository.save(task);

        taskHistoryService.recordChange(
                taskId,
                currentUserService.getCurrentUserId(),
                TaskChangeType.DEADLINE,
                oldDeadline != null ? oldDeadline.toString() : null,
                newDeadline != null ? newDeadline.toString() : null
        );

        taskEventPublisher.publishTaskDeadlineChanged(
                updatedTask,
                oldDeadline,
                newDeadline
        );

        return taskMapper.toResponse(updatedTask);
    }
}