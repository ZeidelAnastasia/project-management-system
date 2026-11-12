package com.projectmanagement.task.service.impl;

import com.projectmanagement.task.dto.request.UpdateTaskAssigneeRequest;
import com.projectmanagement.task.dto.response.TaskResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.entity.enums.TaskChangeType;
import com.projectmanagement.task.exception.AssigneeNotProjectMemberException;
import com.projectmanagement.task.exception.TaskNotFoundException;
import com.projectmanagement.task.mapper.TaskMapper;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.CurrentUserService;
import com.projectmanagement.task.client.ProjectAccessClient;
import com.projectmanagement.task.security.TaskAuthorizationService;
import com.projectmanagement.task.service.TaskAssignmentService;
import com.projectmanagement.task.service.TaskEventPublisher;
import com.projectmanagement.task.service.TaskHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TaskAuthorizationService taskAuthorizationService;
    private final ProjectAccessClient projectAccessClient;
    private final TaskHistoryService taskHistoryService;
    private final TaskEventPublisher taskEventPublisher;

    @Override
    @Transactional
    public TaskResponse updateAssignee(UUID taskId, UpdateTaskAssigneeRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + taskId + " не найдена"));

        taskAuthorizationService.checkCanManageTask(task);

        UUID newAssigneeId = request.assigneeId();
        UUID oldAssigneeId = task.getAssigneeId();

        if (newAssigneeId != null
                && !projectAccessClient.isProjectMember(task.getProjectId(), newAssigneeId)) {
            throw new AssigneeNotProjectMemberException("Assignee not project member");
        }

        if (java.util.Objects.equals(oldAssigneeId, newAssigneeId)) {
            return taskMapper.toResponse(task);
        }

        task.setAssigneeId(newAssigneeId);
        Task updatedTask = taskRepository.save(task);

        taskHistoryService.recordChange(
                taskId,
                currentUserService.getCurrentUserId(),
                TaskChangeType.ASSIGNEE,
                oldAssigneeId != null ? oldAssigneeId.toString() : null,
                newAssigneeId != null ? newAssigneeId.toString() : null
        );

        taskEventPublisher.publishTaskAssigned(
                updatedTask,
                oldAssigneeId,
                newAssigneeId
        );

        return taskMapper.toResponse(updatedTask);
    }
}