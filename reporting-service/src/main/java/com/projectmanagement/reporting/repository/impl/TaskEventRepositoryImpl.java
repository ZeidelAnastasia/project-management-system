
package com.projectmanagement.reporting.repository.impl;

import com.projectmanagement.reporting.entity.TaskEventEntity;
import com.projectmanagement.reporting.repository.TaskEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TaskEventRepositoryImpl implements TaskEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(TaskEventEntity event) {
        String sql = """
                INSERT INTO task_events (
                    event_id, event_type, task_id, project_id,
                    assignee_id, old_status, new_status,
                    priority, due_date, occurred_at, received_at
                )
                VALUES (
                    :eventId, :eventType, :taskId, :projectId,
                    :assigneeId, :oldStatus, :newStatus,
                    :priority, :dueDate, :occurredAt, :receivedAt
                )
                """;

        jdbcTemplate.update(sql, mapParameters(event));
    }

    @Override
    public void saveAll(List<TaskEventEntity> events) {
        if (events.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO task_events (
                    event_id, event_type, task_id, project_id,
                    assignee_id, old_status, new_status,
                    priority, due_date, occurred_at, received_at
                )
                VALUES (
                    :eventId, :eventType, :taskId, :projectId,
                    :assigneeId, :oldStatus, :newStatus,
                    :priority, :dueDate, :occurredAt, :receivedAt
                )
                """;

        jdbcTemplate.batchUpdate(
                sql,
                events.stream()
                        .map(this::mapParameters)
                        .toArray(MapSqlParameterSource[]::new)
        );
    }

    @Override
    public List<TaskEventEntity> findByProjectId(UUID projectId) {
        String sql = """
                SELECT *
                FROM task_events
                WHERE project_id = :projectId
                ORDER BY occurred_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("projectId", projectId),
                (rs, rowNum) -> mapEntity(rs)
        );
    }

    @Override
    public List<TaskEventEntity> findByAssigneeId(UUID assigneeId) {
        String sql = """
                SELECT *
                FROM task_events
                WHERE assignee_id = :assigneeId
                ORDER BY occurred_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("assigneeId", assigneeId),
                (rs, rowNum) -> mapEntity(rs)
        );
    }

    @Override
    public List<TaskEventEntity> findByProjectIdAndPeriod(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
                SELECT *
                FROM task_events
                WHERE project_id = :projectId
                  AND occurred_at >= :startDate
                  AND occurred_at < :endDate
                ORDER BY occurred_at DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("projectId", projectId)
                .addValue("startDate", Timestamp.valueOf(startDate))
                .addValue("endDate", Timestamp.valueOf(endDate));

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> mapEntity(rs)
        );
    }

    @Override
    public long countCreatedTasks(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
                SELECT count()
                FROM task_events
                WHERE project_id = :projectId
                  AND event_type = 'TaskCreated'
                  AND occurred_at >= :startDate
                  AND occurred_at < :endDate
                """;

        return count(sql, projectId, startDate, endDate);
    }

    @Override
    public long countCompletedTasks(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
                SELECT count()
                FROM task_events
                WHERE project_id = :projectId
                  AND event_type = 'TaskStatusChanged'
                  AND new_status = 'COMPLETED'
                  AND occurred_at >= :startDate
                  AND occurred_at < :endDate
                """;

        return count(sql, projectId, startDate, endDate);
    }

    @Override
    public long countOverdueTasks(UUID projectId, LocalDate date) {
        String sql = """
                SELECT count()
                FROM (
                    SELECT
                        task_id,
                        argMax(due_date, occurred_at) AS latest_due_date,
                        argMax(new_status, occurred_at) AS latest_status
                    FROM task_events
                    WHERE project_id = :projectId
                    GROUP BY task_id
                )
                WHERE latest_due_date < :date
                  AND latest_status != 'COMPLETED'
                """;

        return jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("date", Date.valueOf(date)),
                Long.class
        );
    }

    private long count(
            String sql,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("startDate", Timestamp.valueOf(startDate))
                        .addValue("endDate", Timestamp.valueOf(endDate)),
                Long.class
        );
    }

    private MapSqlParameterSource mapParameters(TaskEventEntity event) {
        return new MapSqlParameterSource()
                .addValue("eventId", event.getEventId())
                .addValue("eventType", event.getEventType())
                .addValue("taskId", event.getTaskId())
                .addValue("projectId", event.getProjectId())
                .addValue("assigneeId", event.getAssigneeId())
                .addValue("oldStatus", event.getOldStatus())
                .addValue("newStatus", event.getNewStatus())
                .addValue("priority", event.getPriority())
                .addValue("dueDate", event.getDueDate())
                .addValue("occurredAt", event.getOccurredAt())
                .addValue("receivedAt", event.getReceivedAt());
    }

    private TaskEventEntity mapEntity(java.sql.ResultSet rs)
            throws java.sql.SQLException {
        return TaskEventEntity.builder()
                .eventId(UUID.fromString(rs.getString("event_id")))
                .eventType(rs.getString("event_type"))
                .taskId(UUID.fromString(rs.getString("task_id")))
                .projectId(UUID.fromString(rs.getString("project_id")))
                .assigneeId(rs.getString("assignee_id") == null
                        ? null
                        : UUID.fromString(rs.getString("assignee_id")))
                .oldStatus(rs.getString("old_status"))
                .newStatus(rs.getString("new_status"))
                .priority(rs.getString("priority"))
                .dueDate(rs.getDate("due_date") == null
                        ? null
                        : rs.getDate("due_date").toLocalDate())
                .occurredAt(rs.getTimestamp("occurred_at").toLocalDateTime())
                .receivedAt(rs.getTimestamp("received_at").toLocalDateTime())
                .build();
    }
}
