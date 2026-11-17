package com.projectmanagement.intelligence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "project_risk_assessments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRiskAssessmentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "delay_probability", nullable = false)
    private Double delayProbability;

    @Column(name = "predicted_completion_date")
    private LocalDate predictedCompletionDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assessment_details", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> assessmentDetails = new HashMap<>();

    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    @Column(name = "model_version", nullable = false, length = 100)
    private String modelVersion;

    @OneToMany(
            mappedBy = "assessment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RiskFactorEntity> riskFactors = new ArrayList<>();
}