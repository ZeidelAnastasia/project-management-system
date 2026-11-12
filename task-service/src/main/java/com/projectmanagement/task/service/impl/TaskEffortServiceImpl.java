package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.UpdateTaskEffortRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskEffortService;
import com.projectmanagement.task.service.TaskEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskEffortServiceImpl implements TaskEffortService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskAuthorizationService taskAuthorizationService;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    @Transactional
    public TaskResponse updateEffort(UUID taskId, UpdateTaskEffortRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        taskAuthorizationService.checkCanManageTask(task);

        Integer oldEffort = task.getActualEffort();
        Integer newEffort = request.actualEffort();

        if (Objects.equals(oldEffort, newEffort)) {
            return taskMapper.toResponse(task);
        }

        task.setActualEffort(newEffort);
        Task updatedTask = taskRepository.save(task);

        taskEventPublisher.publishTaskEffortUpdated(updatedTask, oldEffort, newEffort);

        return taskMapper.toResponse(updatedTask);
    }
}