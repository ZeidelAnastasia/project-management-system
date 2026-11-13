package com.projectmanagement.task.client;

import com.projectmanagement.task.client.dto.TaskAccessResponse;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.repository.TaskRepository;
import com.projectmanagement.task.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAccessServiceImpl implements TaskAccessService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final ProjectAccessClient projectAccessClient;

    @Override
    public TaskAccessResponse getTaskAccess(UUID taskId, UUID userId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        if (!currentUserId.equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return new TaskAccessResponse(false, null, null, false, false);
        }

        UUID projectId = task.getProjectId();

        boolean projectMember = projectAccessClient.isProjectMember(projectId, userId);
        boolean projectManager = projectAccessClient.isProjectManager(projectId, userId);

        return new TaskAccessResponse(
                true,
                projectId,
                task.getAssigneeId(),
                projectMember,
                projectManager
        );
    }
}