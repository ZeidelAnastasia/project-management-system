package com.projectmanagement.project.controller;

import com.projectmanagement.project.controller.api.ProjectApi;
import com.projectmanagement.project.dto.request.CompleteProjectRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.security.CurrentUser;
import com.projectmanagement.project.security.CurrentUserProvider;
import com.projectmanagement.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {

    private final ProjectService projectService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<ProjectResponse> createProject(
            CreateProjectRequest request
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        ProjectResponse response = projectService.createProject(
                request,
                currentUser.id(),
                currentUser.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProjectResponse> getProject(UUID projectId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.getProject(
                        projectId,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.getProjects(
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<ProjectResponse> updateProject(
            UUID projectId,
            UpdateProjectRequest request
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.updateProject(
                        projectId,
                        request,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<ProjectResponse> archiveProject(UUID projectId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.archiveProject(
                        projectId,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<ProjectResponse> restoreProject(UUID projectId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.restoreProject(
                        projectId,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }

    @Override
    public ResponseEntity<ProjectResponse> completeProject(
            UUID projectId,
            CompleteProjectRequest request
    ) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        return ResponseEntity.ok(
                projectService.completeProject(
                        projectId,
                        request,
                        currentUser.id(),
                        currentUser.role()
                )
        );
    }
}