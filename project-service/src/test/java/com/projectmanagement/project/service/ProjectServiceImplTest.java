package com.projectmanagement.project.service;

import com.projectmanagement.project.client.TaskServiceClient;
import com.projectmanagement.project.client.dto.TaskResponse;
import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.ProjectMember;
import com.projectmanagement.project.entity.enums.ProjectRole;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import com.projectmanagement.project.event.ProjectEventOutboxService;
import com.projectmanagement.project.exception.InvalidProjectStateException;
import com.projectmanagement.project.exception.ProjectAccessDeniedException;
import com.projectmanagement.project.exception.ProjectNotFoundException;
import com.projectmanagement.project.mapper.ProjectMapper;
import com.projectmanagement.project.repository.ProjectMemberRepository;
import com.projectmanagement.project.repository.ProjectRepository;
import com.projectmanagement.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskServiceClient taskServiceClient;
    @Mock
    private ProjectEventOutboxService projectEventOutboxService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID userId;
    private UUID projectId;
    private Project project;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setStatus(ProjectStatus.ACTIVE);

        projectResponse = new ProjectResponse(
                projectId,
                "Test Project",
                "Description",
                ProjectStatus.ACTIVE,
                userId,
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    private TaskResponse createTaskResponse(String status) {
        return new TaskResponse(
                UUID.randomUUID(),
                projectId,
                userId,
                "Task Title",
                status
        );
    }

    @Nested
    @DisplayName("Create Project Tests")
    class CreateProjectTests {

        @Test
        @DisplayName("Should successfully create project when user is PROJECT_MANAGER")
        void createProject_Success() {
            CreateProjectRequest request = new CreateProjectRequest("Test Project", "Description");
            when(projectMapper.toEntity(request)).thenReturn(project);
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse response = projectService.createProject(request, userId, "ROLE_PROJECT_MANAGER");

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(projectId);

            ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
            verify(projectMemberRepository).save(memberCaptor.capture());
            ProjectMember savedMember = memberCaptor.getValue();

            assertThat(savedMember.getProjectRole()).isEqualTo(ProjectRole.MANAGER);
            assertThat(savedMember.getId().getProjectId()).isEqualTo(projectId);
            assertThat(savedMember.getId().getUserId()).isEqualTo(userId);

            verify(projectEventOutboxService).publishProjectCreated(project);
        }

        @Test
        @DisplayName("Should throw ProjectAccessDeniedException when user does not have permission to create project")
        void createProject_AccessDenied() {
            CreateProjectRequest request = new CreateProjectRequest("Test Project", "Description");

            assertThatThrownBy(() -> projectService.createProject(request, userId, "ROLE_DEVELOPER"))
                    .isInstanceOf(ProjectAccessDeniedException.class)
                    .hasMessageContaining("Only administrators and project managers can create projects");

            verifyNoInteractions(projectRepository, projectMemberRepository, projectEventOutboxService);
        }
    }

    @Nested
    @DisplayName("Get Project(s) Tests")
    class GetProjectTests {

        @Test
        @DisplayName("Should return project when user is member")
        void getProject_Success_WhenUserIsMember() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, userId)).thenReturn(true);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.getProject(projectId, userId, "DEVELOPER");

            assertThat(result).isNotNull();
            verify(projectMemberRepository).existsByProject_IdAndUserId(projectId, userId);
        }

        @Test
        @DisplayName("Should return project without membership check when user is ADMIN")
        void getProject_Success_WhenUserIsAdmin() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.getProject(projectId, userId, "ROLE_ADMIN");

            assertThat(result).isNotNull();
            verifyNoInteractions(projectMemberRepository);
        }

        @Test
        @DisplayName("Should throw ProjectAccessDeniedException when user is not member and not admin")
        void getProject_AccessDenied() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, userId)).thenReturn(false);

            assertThatThrownBy(() -> projectService.getProject(projectId, userId, "DEVELOPER"))
                    .isInstanceOf(ProjectAccessDeniedException.class);
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project does not exist")
        void getProject_NotFound() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.getProject(projectId, userId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectNotFoundException.class);
        }

        @Test
        @DisplayName("getProjects: Should return all projects for ADMIN")
        void getProjects_Admin() {
            when(projectRepository.findAll()).thenReturn(List.of(project));
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            List<ProjectResponse> result = projectService.getProjects(userId, "ADMIN");

            assertThat(result).hasSize(1);
            verify(projectRepository).findAll();
            verifyNoInteractions(projectMemberRepository);
        }

        @Test
        @DisplayName("getProjects: Should return only user projects for regular user")
        void getProjects_RegularUser() {
            ProjectMember member = new ProjectMember();
            member.setProject(project);

            when(projectMemberRepository.findAllByUserId(userId)).thenReturn(List.of(member));
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            List<ProjectResponse> result = projectService.getProjects(userId, "DEVELOPER");

            assertThat(result).hasSize(1);
            verify(projectMemberRepository).findAllByUserId(userId);
            verify(projectRepository, never()).findAll();
        }
    }

    @Nested
    @DisplayName("Update Project Tests")
    class UpdateProjectTests {

        private final UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated Desc");

        @Test
        @DisplayName("Should successfully update project when user is project MANAGER")
        void updateProject_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserIdAndProjectRole(projectId, userId, ProjectRole.MANAGER))
                    .thenReturn(true);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            ProjectResponse result = projectService.updateProject(projectId, request, userId, "PROJECT_MANAGER");

            assertThat(result).isNotNull();
            verify(projectMapper).updateEntity(request, project);
            verify(projectEventOutboxService).publishProjectUpdated(project);
        }

        @Test
        @DisplayName("Should throw InvalidProjectStateException when updating non-active project")
        void updateProject_NotActive() {
            project.setStatus(ProjectStatus.ARCHIVED);
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectService.updateProject(projectId, request, userId, "ADMIN"))
                    .isInstanceOf(InvalidProjectStateException.class)
                    .hasMessageContaining("Only active projects can be modified");
        }
    }

    @Nested
    @DisplayName("Archive and Restore Tests")
    class ArchiveAndRestoreTests {

        @Test
        @DisplayName("Should archive active project successfully")
        void archiveProject_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            projectService.archiveProject(projectId, userId, "ROLE_ADMIN");

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
            assertThat(project.getArchivedAt()).isNotNull();
            verify(projectEventOutboxService).publishProjectArchived(project);
        }

        @Test
        @DisplayName("Should restore archived project successfully")
        void restoreProject_Success() {
            project.setStatus(ProjectStatus.ARCHIVED);
            project.setArchivedAt(LocalDateTime.now());

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            projectService.restoreProject(projectId, userId, "ROLE_ADMIN");

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
            assertThat(project.getArchivedAt()).isNull();
            verify(projectEventOutboxService).publishProjectRestored(project);
        }

        @Test
        @DisplayName("Should throw InvalidProjectStateException when restoring non-archived project")
        void restoreProject_InvalidState() {
            project.setStatus(ProjectStatus.ACTIVE);
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectService.restoreProject(projectId, userId, "ROLE_ADMIN"))
                    .isInstanceOf(InvalidProjectStateException.class)
                    .hasMessageContaining("Only archived projects can be restored");
        }
    }

    @Nested
    @DisplayName("Complete Project Tests")
    class CompleteProjectTests {

        @Test
        @DisplayName("Should complete project when all tasks are DONE")
        void completeProject_AllTasksDone_Success() {
            CompleteProjectRequest request = new CompleteProjectRequest(false);
            TaskResponse doneTask = createTaskResponse("DONE");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(taskServiceClient.getProjectTasks(projectId)).thenReturn(List.of(doneTask));
            when(projectMemberRepository.existsByProject_IdAndUserIdAndProjectRole(projectId, userId, ProjectRole.MANAGER))
                    .thenReturn(true);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            projectService.completeProject(projectId, request, userId, "PROJECT_MANAGER");

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
            assertThat(project.getCompletedAt()).isNotNull();
            verify(projectEventOutboxService).publishProjectCompleted(project);
        }

        @Test
        @DisplayName("Should allow ADMIN to complete project with unfinished tasks when confirmed")
        void completeProject_WithUnfinishedTasks_AdminConfirmed_Success() {
            CompleteProjectRequest request = new CompleteProjectRequest(true);
            TaskResponse openTask = createTaskResponse("IN_PROGRESS");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(taskServiceClient.getProjectTasks(projectId)).thenReturn(List.of(openTask));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toResponse(project)).thenReturn(projectResponse);

            projectService.completeProject(projectId, request, userId, "ROLE_ADMIN");

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
            verify(projectEventOutboxService).publishProjectCompleted(project);
        }

        @Test
        @DisplayName("Should throw ProjectAccessDeniedException when non-admin tries to complete project with unfinished tasks")
        void completeProject_WithUnfinishedTasks_NonAdmin_AccessDenied() {
            CompleteProjectRequest request = new CompleteProjectRequest(true);
            TaskResponse openTask = createTaskResponse("TODO");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(taskServiceClient.getProjectTasks(projectId)).thenReturn(List.of(openTask));

            assertThatThrownBy(() -> projectService.completeProject(projectId, request, userId, "PROJECT_MANAGER"))
                    .isInstanceOf(ProjectAccessDeniedException.class)
                    .hasMessageContaining("Only an administrator can complete a project with unfinished tasks");
        }

        @Test
        @DisplayName("Should throw InvalidProjectStateException when admin tries to complete project with unfinished tasks without confirmation")
        void completeProject_WithUnfinishedTasks_AdminNotConfirmed_ThrowsException() {
            CompleteProjectRequest request = new CompleteProjectRequest(false);
            TaskResponse openTask = createTaskResponse("TODO");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(taskServiceClient.getProjectTasks(projectId)).thenReturn(List.of(openTask));

            assertThatThrownBy(() -> projectService.completeProject(projectId, request, userId, "ADMIN"))
                    .isInstanceOf(InvalidProjectStateException.class)
                    .hasMessageContaining("Confirmation is required to complete a project with unfinished tasks");
        }
    }
}