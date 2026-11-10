
package com.projectmanagement.project.repository;

import com.projectmanagement.project.entity.Project;
import com.projectmanagement.project.entity.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByCreatedBy(UUID createdBy);

    List<Project> findAllByCreatedByAndStatus(
            UUID createdBy,
            ProjectStatus status
    );

    List<Project> findAllByStatus(ProjectStatus status);

    boolean existsByIdAndCreatedBy(UUID id, UUID createdBy);
}
