package com.projectmanagement.project.mapper;

import com.projectmanagement.project.dto.request.AddProjectMemberRequest;
import com.projectmanagement.project.dto.request.CreateProjectRequest;
import com.projectmanagement.project.dto.request.UpdateProjectRequest;
import com.projectmanagement.project.dto.response.ProjectMemberResponse;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.ProjectMember;
import com.projectmanagement.project.entity.ProjectMemberId;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        return project;
    }

    public void updateEntity(UpdateProjectRequest request, Project project) {
        project.setName(request.name());
        project.setDescription(request.description());
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedBy(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getArchivedAt(),
                project.getCompletedAt()
        );
    }

    public ProjectMember toMemberEntity(
            AddProjectMemberRequest request,
            Project project
    ) {
        ProjectMember member = new ProjectMember();
        member.setId(new ProjectMemberId(project.getId(), request.userId()));
        member.setProject(project);
        member.setProjectRole(request.projectRole());
        return member;
    }

    public ProjectMemberResponse toMemberResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId().getUserId(),
                member.getProjectRole(),
                member.getJoinedAt()
        );
    }
}