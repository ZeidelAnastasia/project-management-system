package com.projectmanagement.project.service.impl;

import com.projectmanagement.project.client.TaskServiceClient;
import com.projectmanagement.project.client.dto.TaskResponse;
import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.ProjectMember;
import com.projectmanagement.project.entity.enums.ProjectRole;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import com.projectmanagement.project.exception.InvalidProjectStateException;
import com.projectmanagement.project.exception.ProjectAccessDeniedException;
import com.projectmanagement.project.exception.ProjectConflictException;
import com.projectmanagement.project.exception.ProjectMemberNotFoundException;
import com.projectmanagement.project.exception.ProjectNotFoundException;
import com.projectmanagement.project.mapper.ProjectMapper;
import com.projectmanagement.project.repository.ProjectMemberRepository;
import com.projectmanagement.project.repository.ProjectRepository;
import com.projectmanagement.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;
    private final TaskServiceClient taskServiceClient;

    @Override
    public ProjectMemberResponse addMember(
            UUID projectId,
            AddProjectMemberRequest request,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);
        ensureCanManageProject(project, currentUserId, currentUserRole);
        ensureActive(project);

        if (projectMemberRepository.existsByProject_IdAndUserId(
                projectId,
                request.userId()
        )) {
            throw new ProjectConflictException(
                    "User is already a member of this project"
            );
        }

        ProjectMember member = projectMapper.toMemberEntity(request, project);
        ProjectMember savedMember = projectMemberRepository.save(member);

        return projectMapper.toMemberResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMembers(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    ) {
        findProject(projectId);
        ensureProjectAccess(projectId, currentUserId, currentUserRole);

        return projectMemberRepository.findAllByProject_Id(projectId)
                .stream()
                .map(projectMapper::toMemberResponse)
                .toList();
    }

    @Override
    public void removeMember(
            UUID projectId,
            UUID userId,
            UUID reassignToUserId,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);
        ensureCanManageProject(project, currentUserId, currentUserRole);
        ensureActive(project);

        ProjectMember member = findMember(projectId, userId);

        if (member.getProjectRole() == ProjectRole.MANAGER) {
            long managersCount = projectMemberRepository
                    .findAllByProject_Id(projectId)
                    .stream()
                    .filter(projectMember ->
                            projectMember.getProjectRole() == ProjectRole.MANAGER)
                    .count();

            if (managersCount <= 1) {
                throw new ProjectConflictException(
                        "The last project manager cannot be removed"
                );
            }
        }

        List<TaskResponse> unfinishedTasks = taskServiceClient
                .getTasksAssignedToUser(projectId, userId)
                .stream()
                .filter(task -> !"DONE".equalsIgnoreCase(task.status()))
                .toList();

        if (!unfinishedTasks.isEmpty()) {
            if (reassignToUserId == null) {
                throw new ProjectConflictException(
                        "Unfinished tasks must be reassigned before removing the member"
                );
            }

            if (userId.equals(reassignToUserId)) {
                throw new ProjectConflictException(
                        "Tasks cannot be reassigned to the member being removed"
                );
            }

            if (!projectMemberRepository.existsByProject_IdAndUserId(
                    projectId,
                    reassignToUserId
            )) {
                throw new ProjectConflictException(
                        "The new assignee must be a member of this project"
                );
            }

            unfinishedTasks.forEach(task ->
                    taskServiceClient.updateTaskAssignee(
                            task.id(),
                            reassignToUserId
                    )
            );
        }

        projectMemberRepository.delete(member);
    }

    @Override
    public ProjectMemberResponse updateMemberRole(
            UUID projectId,
            UUID userId,
            ProjectRole projectRole,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);
        ensureCanManageProject(project, currentUserId, currentUserRole);
        ensureActive(project);

        ProjectMember member = findMember(projectId, userId);

        if (member.getProjectRole() == ProjectRole.MANAGER
                && projectRole != ProjectRole.MANAGER) {
            long managersCount = projectMemberRepository
                    .findAllByProject_Id(projectId)
                    .stream()
                    .filter(projectMember ->
                            projectMember.getProjectRole() == ProjectRole.MANAGER)
                    .count();

            if (managersCount <= 1) {
                throw new ProjectConflictException(
                        "The last project manager cannot be demoted"
                );
            }
        }

        member.setProjectRole(projectRole);

        return projectMapper.toMemberResponse(
                projectMemberRepository.save(member)
        );
    }

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Проект с id" + projectId + " не найден"));
    }

    private ProjectMember findMember(UUID projectId, UUID userId) {
        return projectMemberRepository
                .findByProject_IdAndUserId(projectId, userId)
                .orElseThrow(() ->
                        new ProjectMemberNotFoundException("В проекте " + projectId + " нет участника " + userId)
                );
    }

    private void ensureProjectAccess(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    ) {
        if (isAdmin(currentUserRole)) {
            return;
        }

        if (!projectMemberRepository.existsByProject_IdAndUserId(
                projectId,
                currentUserId
        )) {
            throw new ProjectAccessDeniedException(
                    "You do not have access to this project"
            );
        }
    }

    private void ensureCanManageProject(
            Project project,
            UUID currentUserId,
            String currentUserRole
    ) {
        if (isAdmin(currentUserRole)) {
            return;
        }

        if (!isProjectManager(currentUserRole)) {
            throw new ProjectAccessDeniedException(
                    "Only project managers and administrators can manage project members"
            );
        }

        boolean isManager = projectMemberRepository
                .existsByProject_IdAndUserIdAndProjectRole(
                        project.getId(),
                        currentUserId,
                        ProjectRole.MANAGER
                );

        if (!isManager) {
            throw new ProjectAccessDeniedException(
                    "You are not a manager of this project"
            );
        }
    }

    private void ensureActive(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new InvalidProjectStateException(
                    "Members can only be managed in active projects"
            );
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(normalizeRole(role));
    }

    private boolean isProjectManager(String role) {
        return "PROJECT_MANAGER".equalsIgnoreCase(normalizeRole(role));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }

        return role.startsWith("ROLE_")
                ? role.substring(5)
                : role;
    }
}