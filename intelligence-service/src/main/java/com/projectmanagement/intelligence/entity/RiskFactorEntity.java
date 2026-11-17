package com.projectmanagement.intelligence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "risk_factors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private ProjectRiskAssessmentEntity assessment;

    @Column(name = "factor_type", nullable = false, length = 100)
    private String factorType;

    @Column(name = "factor_value", nullable = false)
    private Double factorValue;

    @Column(name = "impact_score", nullable = false)
    private Double impactScore;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}