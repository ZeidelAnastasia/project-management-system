
package com.projectmanagement.task.security;

import com.projectmanagement.task.client.ProjectAccessClient;
import com.projectmanagement.task.entity.Task;
import com.projectmanagement.task.client.ProjectAccessClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAuthorizationService {

    private final CurrentUserService currentUserService;
    private final ProjectAccessClient projectAccessClient;

    public void checkCanViewTask(Task task) {
        UUID userId = currentUserService.getCurrentUserId();

        if (currentUserService.isAdmin()) {
            return;
        }

        if (currentUserService.hasRole("PROJECT_MANAGER")
                && projectAccessClient.isProjectManager(task.getProjectId(), userId)) {
            return;
        }

        if (currentUserService.hasRole("EMPLOYEE")
                && task.getAssigneeId() != null
                && task.getAssigneeId().equals(userId)
                && projectAccessClient.isProjectMember(task.getProjectId(), userId)) {
            return;
        }

        throw new AccessDeniedException("Access to this task is denied");
    }

    public void checkCanChangeStatus(Task task) {
        UUID userId = currentUserService.getCurrentUserId();

        if (currentUserService.isAdmin()) {
            return;
        }

        if (currentUserService.hasRole("PROJECT_MANAGER")
                && projectAccessClient.isProjectManager(task.getProjectId(), userId)) {
            return;
        }

        if (currentUserService.hasRole("EMPLOYEE")
                && userId.equals(task.getAssigneeId())
                && projectAccessClient.isProjectMember(task.getProjectId(), userId)) {
            return;
        }

        throw new AccessDeniedException("You cannot change this task status");
    }

    public void checkCanManageTask(Task task) {
        UUID userId = currentUserService.getCurrentUserId();

        if (currentUserService.isAdmin()) {
            return;
        }

        if (currentUserService.hasRole("PROJECT_MANAGER")
                && projectAccessClient.isProjectManager(task.getProjectId(), userId)) {
            return;
        }

        throw new AccessDeniedException("You cannot manage this task");
    }

    public void checkCanAccessProject(UUID projectId) {
        UUID userId = currentUserService.getCurrentUserId();

        if (currentUserService.isAdmin()) {
            return;
        }

        if (currentUserService.hasRole("PROJECT_MANAGER")
                && projectAccessClient.isProjectManager(projectId, userId)) {
            return;
        }

        if (currentUserService.hasRole("EMPLOYEE")
                && projectAccessClient.isProjectMember(projectId, userId)) {
            return;
        }

        throw new AccessDeniedException("Access to this project is denied");
    }

    public void checkCanManageProject(UUID projectId) {
        if (currentUserService.isAdmin()) {
            return;
        }

        UUID userId = currentUserService.getCurrentUserId();

        if (!projectAccessClient.isProjectManager(projectId, userId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
