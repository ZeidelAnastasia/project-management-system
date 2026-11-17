package com.projectmanagement.intelligence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_snapshots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSnapshotEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "task_id", nullable = false, unique = true)
    private UUID taskId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String priority;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimated_effort")
    private Integer estimatedEffort;

    @Column(name = "actual_effort")
    private Integer actualEffort;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> labels = new ArrayList<>();

    @Column(name = "status_change_count", nullable = false)
    @Builder.Default
    private Integer statusChangeCount = 0;

    @Column(name = "deadline_change_count", nullable = false)
    @Builder.Default
    private Integer deadlineChangeCount = 0;

    @Column(name = "reassignment_count", nullable = false)
    @Builder.Default
    private Integer reassignmentCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String corpus;

    @Column(name = "num_comments", nullable = false)
    @Builder.Default
    private Integer numComments = 0;

    @Column(name = "num_activities", nullable = false)
    @Builder.Default
    private Integer numActivities = 0;
}