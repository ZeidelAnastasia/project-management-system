package com.projectmanagement.project.service;

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
import com.projectmanagement.project.mapper.ProjectMapper;
import com.projectmanagement.project.repository.ProjectMemberRepository;
import com.projectmanagement.project.repository.ProjectRepository;
import com.projectmanagement.project.service.impl.ProjectMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskServiceClient taskServiceClient;

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    private UUID projectId;
    private UUID currentUserId;
    private UUID targetUserId;
    private Project project;
    private ProjectMember projectMember;
    private ProjectMemberResponse projectMemberResponse;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setStatus(ProjectStatus.ACTIVE);

        projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setProjectRole(ProjectRole.MEMBER);

        projectMemberResponse = new ProjectMemberResponse(
                targetUserId,
                ProjectRole.MEMBER,
                LocalDateTime.now()
        );
    }

    private TaskResponse createTaskResponse(UUID id, UUID assigneeId, String status) {
        return new TaskResponse(
                id,
                projectId,
                assigneeId,
                "Task title",
                status
        );
    }

    @Nested
    @DisplayName("addMember Tests")
    class AddMemberTests {

        private final AddProjectMemberRequest request = new AddProjectMemberRequest(UUID.randomUUID(), ProjectRole.MEMBER);

        @Test
        @DisplayName("Should successfully add member when manager manages active project")
        void addMember_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserIdAndProjectRole(projectId, currentUserId, ProjectRole.MANAGER))
                    .thenReturn(true);
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, request.userId())).thenReturn(false);
            when(projectMapper.toMemberEntity(request, project)).thenReturn(projectMember);
            when(projectMemberRepository.save(projectMember)).thenReturn(projectMember);
            when(projectMapper.toMemberResponse(projectMember)).thenReturn(projectMemberResponse);

            ProjectMemberResponse result = projectMemberService.addMember(projectId, request, currentUserId, "PROJECT_MANAGER");

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(targetUserId);
            verify(projectMemberRepository).save(projectMember);
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when user is already a project member")
        void addMember_AlreadyMember_ThrowsConflict() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, request.userId())).thenReturn(true);

            assertThatThrownBy(() -> projectMemberService.addMember(projectId, request, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("User is already a member of this project");

            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidProjectStateException when project is not active")
        void addMember_ProjectNotActive_ThrowsException() {
            project.setStatus(ProjectStatus.ARCHIVED);
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

            assertThatThrownBy(() -> projectMemberService.addMember(projectId, request, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(InvalidProjectStateException.class)
                    .hasMessageContaining("Members can only be managed in active projects");
        }

        @Test
        @DisplayName("Should throw ProjectAccessDeniedException when user is not a project manager or admin")
        void addMember_NotManager_AccessDenied() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserIdAndProjectRole(projectId, currentUserId, ProjectRole.MANAGER))
                    .thenReturn(false);

            assertThatThrownBy(() -> projectMemberService.addMember(projectId, request, currentUserId, "ROLE_PROJECT_MANAGER"))
                    .isInstanceOf(ProjectAccessDeniedException.class)
                    .hasMessageContaining("You are not a manager of this project");
        }
    }

    @Nested
    @DisplayName("getMembers Tests")
    class GetMembersTests {

        @Test
        @DisplayName("Should return members when user has access")
        void getMembers_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, currentUserId)).thenReturn(true);
            when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(List.of(projectMember));
            when(projectMapper.toMemberResponse(projectMember)).thenReturn(projectMemberResponse);

            List<ProjectMemberResponse> members = projectMemberService.getMembers(projectId, currentUserId, "USER");

            assertThat(members).hasSize(1);
            assertThat(members.get(0).userId()).isEqualTo(targetUserId);
            verify(projectMemberRepository).findAllByProject_Id(projectId);
        }

        @Test
        @DisplayName("Should return members without membership check for ADMIN")
        void getMembers_Admin_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findAllByProject_Id(projectId)).thenReturn(List.of(projectMember));
            when(projectMapper.toMemberResponse(projectMember)).thenReturn(projectMemberResponse);

            List<ProjectMemberResponse> members = projectMemberService.getMembers(projectId, currentUserId, "ROLE_ADMIN");

            assertThat(members).hasSize(1);
            verify(projectMemberRepository, never()).existsByProject_IdAndUserId(any(), any());
        }

        @Test
        @DisplayName("Should throw ProjectAccessDeniedException when user is not a member and not admin")
        void getMembers_AccessDenied() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, currentUserId)).thenReturn(false);

            assertThatThrownBy(() -> projectMemberService.getMembers(projectId, currentUserId, "USER"))
                    .isInstanceOf(ProjectAccessDeniedException.class)
                    .hasMessageContaining("You do not have access to this project");
        }
    }

    @Nested
    @DisplayName("removeMember Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should successfully remove regular member without unfinished tasks")
        void removeMember_RegularMember_NoTasks_Success() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(taskServiceClient.getTasksAssignedToUser(projectId, targetUserId))
                    .thenReturn(Collections.emptyList());

            projectMemberService.removeMember(projectId, targetUserId, null, currentUserId, "ROLE_ADMIN");

            verify(projectMemberRepository).delete(projectMember);
            verify(taskServiceClient, never()).updateTaskAssignee(any(), any());
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when trying to remove the last manager")
        void removeMember_LastManager_ThrowsConflict() {
            projectMember.setProjectRole(ProjectRole.MANAGER);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(projectMemberRepository.findAllByProject_Id(projectId))
                    .thenReturn(List.of(projectMember)); // Only 1 manager

            assertThatThrownBy(() -> projectMemberService.removeMember(projectId, targetUserId, null, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("The last project manager cannot be removed");

            verify(projectMemberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should reassign tasks and remove member when tasks exist and valid reassignee provided")
        void removeMember_WithUnfinishedTasks_ReassignedSuccessfully() {
            UUID reassignToUserId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            TaskResponse task = createTaskResponse(taskId, targetUserId, "IN_PROGRESS");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(taskServiceClient.getTasksAssignedToUser(projectId, targetUserId))
                    .thenReturn(List.of(task));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, reassignToUserId))
                    .thenReturn(true);

            projectMemberService.removeMember(projectId, targetUserId, reassignToUserId, currentUserId, "ROLE_ADMIN");

            verify(taskServiceClient).updateTaskAssignee(taskId, reassignToUserId);
            verify(projectMemberRepository).delete(projectMember);
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when unfinished tasks exist but reassignToUserId is null")
        void removeMember_WithUnfinishedTasks_NoReassignee_ThrowsConflict() {
            TaskResponse task = createTaskResponse(UUID.randomUUID(), targetUserId, "TODO");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(taskServiceClient.getTasksAssignedToUser(projectId, targetUserId))
                    .thenReturn(List.of(task));

            assertThatThrownBy(() -> projectMemberService.removeMember(projectId, targetUserId, null, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("Unfinished tasks must be reassigned before removing the member");

            verify(projectMemberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when reassigning tasks to the same member being removed")
        void removeMember_ReassignToSameUser_ThrowsConflict() {
            TaskResponse task = createTaskResponse(UUID.randomUUID(), targetUserId, "TODO");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(taskServiceClient.getTasksAssignedToUser(projectId, targetUserId))
                    .thenReturn(List.of(task));

            assertThatThrownBy(() -> projectMemberService.removeMember(projectId, targetUserId, targetUserId, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("Tasks cannot be reassigned to the member being removed");
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when reassignee is not a project member")
        void removeMember_ReassigneeNotMember_ThrowsConflict() {
            UUID reassignToUserId = UUID.randomUUID();
            TaskResponse task = createTaskResponse(UUID.randomUUID(), targetUserId, "TODO");

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(taskServiceClient.getTasksAssignedToUser(projectId, targetUserId))
                    .thenReturn(List.of(task));
            when(projectMemberRepository.existsByProject_IdAndUserId(projectId, reassignToUserId))
                    .thenReturn(false);

            assertThatThrownBy(() -> projectMemberService.removeMember(projectId, targetUserId, reassignToUserId, currentUserId, "ROLE_ADMIN"))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("The new assignee must be a member of this project");
        }
    }

    @Nested
    @DisplayName("updateMemberRole Tests")
    class UpdateMemberRoleTests {

        @Test
        @DisplayName("Should successfully update role when demoting is not violating the last manager rule")
        void updateMemberRole_Success() {
            ProjectMember otherManager = new ProjectMember();
            otherManager.setProjectRole(ProjectRole.MANAGER);

            projectMember.setProjectRole(ProjectRole.MANAGER);

            ProjectMemberResponse updatedResponse = new ProjectMemberResponse(
                    targetUserId,
                    ProjectRole.MEMBER,
                    LocalDateTime.now()
            );

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(projectMemberRepository.findAllByProject_Id(projectId))
                    .thenReturn(List.of(projectMember, otherManager)); // 2 managers exist
            when(projectMemberRepository.save(projectMember)).thenReturn(projectMember);
            when(projectMapper.toMemberResponse(projectMember)).thenReturn(updatedResponse);

            ProjectMemberResponse result = projectMemberService.updateMemberRole(
                    projectId, targetUserId, ProjectRole.MEMBER, currentUserId, "ROLE_ADMIN"
            );

            assertThat(result).isNotNull();
            assertThat(projectMember.getProjectRole()).isEqualTo(ProjectRole.MEMBER);
            verify(projectMemberRepository).save(projectMember);
        }

        @Test
        @DisplayName("Should throw ProjectConflictException when attempting to demote the last manager")
        void updateMemberRole_DemoteLastManager_ThrowsConflict() {
            projectMember.setProjectRole(ProjectRole.MANAGER);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.of(projectMember));
            when(projectMemberRepository.findAllByProject_Id(projectId))
                    .thenReturn(List.of(projectMember)); // Only 1 manager

            assertThatThrownBy(() -> projectMemberService.updateMemberRole(
                    projectId, targetUserId, ProjectRole.MEMBER, currentUserId, "ROLE_ADMIN"
            ))
                    .isInstanceOf(ProjectConflictException.class)
                    .hasMessageContaining("The last project manager cannot be demoted");

            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ProjectMemberNotFoundException when member does not exist")
        void updateMemberRole_MemberNotFound_ThrowsException() {
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProject_IdAndUserId(projectId, targetUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectMemberService.updateMemberRole(
                    projectId, targetUserId, ProjectRole.MEMBER, currentUserId, "ROLE_ADMIN"
            ))
                    .isInstanceOf(ProjectMemberNotFoundException.class);
        }
    }
}