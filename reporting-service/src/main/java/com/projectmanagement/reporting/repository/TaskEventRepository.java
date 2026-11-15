
package com.projectmanagement.reporting.repository;

import com.projectmanagement.reporting.entity.TaskEventEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskEventRepository {

    void save(TaskEventEntity event);

    void saveAll(List<TaskEventEntity> events);

    List<TaskEventEntity> findByProjectId(UUID projectId);

    List<TaskEventEntity> findByAssigneeId(UUID assigneeId);

    List<TaskEventEntity> findByProjectIdAndPeriod(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countCreatedTasks(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countCompletedTasks(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countOverdueTasks(
            UUID projectId,
            LocalDate date
    );
}
