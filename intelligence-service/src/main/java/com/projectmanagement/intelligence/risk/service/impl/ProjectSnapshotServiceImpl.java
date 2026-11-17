package com.projectmanagement.intelligence.risk.service.impl;

import com.projectmanagement.intelligence.entity.ProjectSnapshotEntity;
import com.projectmanagement.intelligence.kafka.event.ProjectSnapshotEvent;
import com.projectmanagement.intelligence.risk.service.ProjectSnapshotService;
import com.projectmanagement.intelligence.repository.ProjectSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectSnapshotServiceImpl implements ProjectSnapshotService {

    private final ProjectSnapshotRepository projectSnapshotRepository;

    @Override
    @Transactional
    public void saveOrUpdate(ProjectSnapshotEvent event) {
        if (event.projectId() == null) {
            throw new IllegalArgumentException("Project ID is required");
        }

        ProjectSnapshotEntity project = projectSnapshotRepository
                .findByProjectId(event.projectId())
                .orElseGet(() -> ProjectSnapshotEntity.builder()
                        .id(UUID.randomUUID())
                        .projectId(event.projectId())
                        .createdAt(event.createdAt() != null
                                ? event.createdAt()
                                : LocalDateTime.now())
                        .build());

        if (event.projectName() != null) {
            project.setProjectName(event.projectName());
        }

        if (event.projectStatus() != null) {
            project.setProjectStatus(event.projectStatus());
        }

        if (event.plannedStartDate() != null) {
            project.setPlannedStartDate(event.plannedStartDate());
        }

        if (event.plannedEndDate() != null) {
            project.setPlannedEndDate(event.plannedEndDate());
        }

        project.setUpdatedAt(event.updatedAt() != null
                ? event.updatedAt()
                : LocalDateTime.now());

        if (project.getProjectStatus() == null) {
            project.setProjectStatus("ACTIVE");
        }

        projectSnapshotRepository.save(project);
    }

    @Override
    @Transactional
    public void updateStatus(UUID projectId, String status) {
        ProjectSnapshotEntity project = projectSnapshotRepository
                .findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project snapshot not found: " + projectId
                ));

        project.setProjectStatus(status);
        project.setUpdatedAt(LocalDateTime.now());

        projectSnapshotRepository.save(project);
    }
}