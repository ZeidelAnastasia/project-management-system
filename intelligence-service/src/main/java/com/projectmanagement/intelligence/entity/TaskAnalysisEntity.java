package com.projectmanagement.intelligence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "task_analysis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnalysisEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "complexity_score", nullable = false)
    private Double complexityScore;

    @Column(name = "complexity_level", nullable = false, length = 50)
    private String complexityLevel;

    @Column(name = "predicted_effort", nullable = false)
    private Double predictedEffort;

    @Column(name = "model_version", nullable = false, length = 100)
    private String modelVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analysis_details", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> analysisDetails = new HashMap<>();
}