package com.projectmanagement.intelligence.repository;

import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskSnapshotRepository extends JpaRepository<TaskSnapshotEntity, UUID> {

    Optional<TaskSnapshotEntity> findByTaskId(UUID taskId);

    List<TaskSnapshotEntity> findAllByProjectId(UUID projectId);

    List<TaskSnapshotEntity> findAllByAssigneeIdAndStatusNot(
            UUID assigneeId,
            String status
    );

    List<TaskSnapshotEntity> findAllByStatus(String status);

    List<TaskSnapshotEntity> findAllByPriorityAndStatus(
            String priority,
            String status
    );

    List<TaskSnapshotEntity> findAllByStatusAndActualEffortIsNotNull(String status);

    void deleteByTaskId(UUID taskId);
}