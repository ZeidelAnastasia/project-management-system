
package com.projectmanagement.reporting.repository.impl;

import com.projectmanagement.reporting.entity.ProjectEventEntity;
import com.projectmanagement.reporting.repository.ProjectEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProjectEventRepositoryImpl implements ProjectEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(ProjectEventEntity event) {
        String sql = """
                INSERT INTO project_events (
                    event_id, event_type, project_id,
                    occurred_at, received_at
                )
                VALUES (
                    :eventId, :eventType, :projectId,
                    :occurredAt, :receivedAt
                )
                """;

        jdbcTemplate.update(sql, mapParameters(event));
    }

    @Override
    public void saveAll(List<ProjectEventEntity> events) {
        if (events.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO project_events (
                    event_id, event_type, project_id,
                    occurred_at, received_at
                )
                VALUES (
                    :eventId, :eventType, :projectId,
                    :occurredAt, :receivedAt
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
    public List<ProjectEventEntity> findByProjectId(UUID projectId) {
        String sql = """
                SELECT *
                FROM project_events
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
    public List<ProjectEventEntity> findByProjectIdAndPeriod(
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
                SELECT *
                FROM project_events
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
    public List<UUID> findDistinctProjectIdsByPeriod(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
            SELECT DISTINCT project_id
            FROM project_events
            WHERE occurred_at >= :startDate
              AND occurred_at < :endDate
            ORDER BY project_id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", Timestamp.valueOf(startDate))
                .addValue("endDate", Timestamp.valueOf(endDate));

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> UUID.fromString(rs.getString("project_id"))
        );
    }


    @Override
    public long countCreatedProjects(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return count("ProjectCreated", startDate, endDate);
    }

    @Override
    public long countCompletedProjects(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return count("ProjectCompleted", startDate, endDate);
    }

    private long count(
            String eventType,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        String sql = """
                SELECT count()
                FROM project_events
                WHERE event_type = :eventType
                  AND occurred_at >= :startDate
                  AND occurred_at < :endDate
                """;

        return jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("eventType", eventType)
                        .addValue("startDate", Timestamp.valueOf(startDate))
                        .addValue("endDate", Timestamp.valueOf(endDate)),
                Long.class
        );
    }

    private MapSqlParameterSource mapParameters(ProjectEventEntity event) {
        return new MapSqlParameterSource()
                .addValue("eventId", event.getEventId())
                .addValue("eventType", event.getEventType())
                .addValue("projectId", event.getProjectId())
                .addValue("occurredAt", event.getOccurredAt())
                .addValue("receivedAt", event.getReceivedAt());
    }

    private ProjectEventEntity mapEntity(java.sql.ResultSet rs)
            throws java.sql.SQLException {
        return ProjectEventEntity.builder()
                .eventId(UUID.fromString(rs.getString("event_id")))
                .eventType(rs.getString("event_type"))
                .projectId(UUID.fromString(rs.getString("project_id")))
                .occurredAt(rs.getTimestamp("occurred_at").toLocalDateTime())
                .receivedAt(rs.getTimestamp("received_at").toLocalDateTime())
                .build();
    }
}
