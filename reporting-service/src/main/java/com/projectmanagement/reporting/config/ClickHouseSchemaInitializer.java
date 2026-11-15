package com.projectmanagement.reporting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClickHouseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createTaskEventsTable();
        createProjectEventsTable();
    }

    private void createTaskEventsTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS task_events
                (
                    event_id UUID,
                    event_type String,
                    task_id UUID,
                    project_id UUID,
                    assignee_id Nullable(UUID),
                    old_status Nullable(String),
                    new_status Nullable(String),
                    priority Nullable(String),
                    due_date Nullable(Date),
                    occurred_at DateTime64(3),
                    received_at DateTime64(3)
                )
                ENGINE = MergeTree()
                PARTITION BY toYYYYMM(occurred_at)
                ORDER BY (project_id, task_id, occurred_at, event_id)
                """);
    }

    private void createProjectEventsTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS project_events
                (
                    event_id UUID,
                    event_type String,
                    project_id UUID,
                    occurred_at DateTime64(3),
                    received_at DateTime64(3)
                )
                ENGINE = MergeTree()
                PARTITION BY toYYYYMM(occurred_at)
                ORDER BY (project_id, occurred_at, event_id)
                """);
    }
}