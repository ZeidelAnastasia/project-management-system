package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.response.TaskHistoryResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.TaskHistory;
import com.projectmanagement.task.entity.enums.TaskChangeType;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskHistoryRepository;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskHistoryServiceImpl implements TaskHistoryService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskMapper taskMapper;
    private final TaskAuthorizationService taskAuthorizationService;

    @Override
    public List<TaskHistoryResponse> getTaskHistory(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        taskAuthorizationService.checkCanViewTask(task);

        List<TaskHistory> history = taskHistoryRepository
                .findAllByTaskIdOrderByChangedAtDesc(taskId);

        return taskMapper.toHistoryResponseList(history);
    }

    @Override
    @Transactional
    public void recordChange(
            UUID taskId,
            UUID changedBy,
            TaskChangeType changeType,
            String oldValue,
            String newValue
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setChangedBy(changedBy);
        history.setChangeType(changeType);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(Instant.now());

        taskHistoryRepository.save(history);
    }
}