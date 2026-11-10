package com.projectmanagement.project.service.impl;

import com.projectmanagement.project.client.TaskServiceClient;
import com.projectmanagement.project.client.dto.TaskResponse;
import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.ProjectMember;
import com.projectmanagement.project.entity.ProjectMemberId;
import com.projectmanagement.project.entity.enums.ProjectRole;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import com.projectmanagement.project.event.ProjectEventOutboxService;
import com.projectmanagement.project.exception.InvalidProjectStateException;
import com.projectmanagement.project.exception.ProjectAccessDeniedException;
import com.projectmanagement.project.exception.ProjectNotFoundException;
import com.projectmanagement.project.mapper.ProjectMapper;
import com.projectmanagement.project.repository.ProjectMemberRepository;
import com.projectmanagement.project.repository.ProjectRepository;
import com.projectmanagement.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;
    private final TaskServiceClient taskServiceClient;
    private final ProjectEventOutboxService projectEventOutboxService;

    @Transactional
    @Override
    public ProjectResponse createProject(
            CreateProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    ) {
        ensureCanCreateProject(currentUserRole);

        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(currentUserId);
        project.setStatus(ProjectStatus.ACTIVE);

        Project savedProject = projectRepository.save(project);

        ProjectMember member = new ProjectMember();
        member.setId(new ProjectMemberId(savedProject.getId(), currentUserId));
        member.setProject(savedProject);
        member.setProjectRole(ProjectRole.MANAGER);

        projectMemberRepository.save(member);

        projectEventOutboxService.publishProjectCreated(savedProject);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);

        ensureProjectAccess(projectId, currentUserId, currentUserRole);

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(
            UUID currentUserId,
            String currentUserRole
    ) {
        if (isAdmin(currentUserRole)) {
            return projectRepository.findAll()
                    .stream()
                    .map(projectMapper::toResponse)
                    .toList();
        }

        return projectMemberRepository.findAllByUserId(currentUserId)
                .stream()
                .map(ProjectMember::getProject)
                .map(projectMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public ProjectResponse updateProject(
            UUID projectId,
            UpdateProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);

        ensureCanManageProject(project, currentUserId, currentUserRole);
        ensureActive(project);

        projectMapper.updateEntity(request, project);
        Project savedProject = projectRepository.save(project);

        projectEventOutboxService.publishProjectUpdated(savedProject);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse archiveProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);

        ensureCanManageProject(project, currentUserId, currentUserRole);
        ensureActive(project);

        project.setStatus(ProjectStatus.ARCHIVED);
        project.setArchivedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);

        projectEventOutboxService.publishProjectArchived(savedProject);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse restoreProject(
            UUID projectId,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);

        ensureCanManageProject(project, currentUserId, currentUserRole);

        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            throw new InvalidProjectStateException(
                    "Only archived projects can be restored"
            );
        }

        project.setStatus(ProjectStatus.ACTIVE);
        project.setArchivedAt(null);
        Project savedProject = projectRepository.save(project);

        projectEventOutboxService.publishProjectRestored(savedProject);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse completeProject(
            UUID projectId,
            CompleteProjectRequest request,
            UUID currentUserId,
            String currentUserRole
    ) {
        Project project = findProject(projectId);

        ensureActive(project);

        List<TaskResponse> tasks = taskServiceClient.getProjectTasks(projectId);

        boolean hasUnfinishedTasks = tasks.stream()
                .anyMatch(task -> !"DONE".equalsIgnoreCase(task.status()));

        if (hasUnfinishedTasks) {
            if (!isAdmin(currentUserRole)) {
                throw new ProjectAccessDeniedException(
                        "Only an administrator can complete a project with unfinished tasks"
                );
            }

            if (!request.confirmed()) {
                throw new InvalidProjectStateException(
                        "Confirmation is required to complete a project with unfinished tasks"
                );
            }
        } else {
            ensureCanManageProject(project, currentUserId, currentUserRole);
        }

        project.setStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        Project savedProject = projectRepository.save(project);

        projectEventOutboxService.publishProjectCompleted(savedProject);
        return projectMapper.toResponse(savedProject);
    }

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Проект с id " + projectId + " не найден"));
    }

    private void ensureCanCreateProject(String currentUserRole) {
        if (!isAdmin(currentUserRole) && !isProjectManager(currentUserRole)) {
            throw new ProjectAccessDeniedException(
                    "Only administrators and project managers can create projects"
            );
        }
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
                    "Only project managers and administrators can manage projects"
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
                    "Only active projects can be modified"
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