package com.projectmanagement.project.controller;

import com.projectmanagement.project.controller.api.ProjectMemberApi;
import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.entity.enums.ProjectRole;
import com.projectmanagement.project.security.CurrentUser;
import com.projectmanagement.project.security.CurrentUserProvider;
import com.projectmanagement.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectMemberController implements ProjectMemberApi {

    private final ProjectMemberService projectMemberService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<ProjectMemberResponse> addMember(
            UUID projectId,
            AddProjectMemberRequest request
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        ProjectMemberResponse response = projectMemberService.addMember(
                projectId,
                request,
                currentUser.id(),
                currentUser.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            UUID projectId
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectMemberService.getMembers(
                        projectId,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<Void> removeMember(
            UUID projectId,
            UUID userId,
            UUID reassignToUserId
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        projectMemberService.removeMember(
                projectId,
                userId,
                reassignToUserId,
                currentUser.id(),
                currentUser.role()
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            UUID projectId,
            UUID userId,
            ProjectRole projectRole
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectMemberService.updateMemberRole(
                        projectId,
                        userId,
                        projectRole,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }
}