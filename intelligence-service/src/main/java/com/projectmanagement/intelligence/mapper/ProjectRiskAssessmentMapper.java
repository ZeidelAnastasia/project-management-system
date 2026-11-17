package com.projectmanagement.intelligence.mapper;

import com.projectmanagement.intelligence.dto.response.ProjectRiskAssessmentResponse;
import com.projectmanagement.intelligence.dto.response.RiskFactorResponse;
import com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectRiskAssessmentMapper {

    public ProjectRiskAssessmentResponse toResponse(
            ProjectRiskAssessmentEntity entity,
            List<String> recommendations
    ) {
        List<RiskFactorResponse> riskFactors = entity.getRiskFactors()
                .stream()
                .map(factor -> new RiskFactorResponse(
                        factor.getId(),
                        factor.getFactorType(),
                        factor.getFactorValue(),
                        factor.getImpactScore(),
                        factor.getDescription()
                ))
                .toList();

        return new ProjectRiskAssessmentResponse(
                entity.getId(),
                entity.getProjectId(),
                entity.getRiskScore(),
                entity.getRiskLevel(),
                entity.getDelayProbability(),
                entity.getPredictedCompletionDate(),
                riskFactors,
                recommendations,
                null,
                entity.getAssessmentDetails(),
                entity.getAssessedAt(),
                entity.getModelVersion()
        );
    }
}