
package com.projectmanagement.reporting.repository;

import com.projectmanagement.reporting.entity.ProjectEventEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProjectEventRepository {

    void save(ProjectEventEntity event);

    void saveAll(List<ProjectEventEntity> events);

    List<ProjectEventEntity> findByProjectId(UUID projectId);

    List<ProjectEventEntity> findByProjectIdAndPeriod(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<UUID> findDistinctProjectIdsByPeriod(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countCreatedProjects(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countCompletedProjects(
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
