
package com.projectmanagement.intelligence.mapper;

import com.projectmanagement.intelligence.dto.response.*;
import com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity;
import com.projectmanagement.intelligence.entity.RiskFactorEntity;
import com.projectmanagement.intelligence.entity.TaskAnalysisEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class IntelligenceMapper {

    public TaskAnalysisResponse toTaskAnalysisResponse(TaskAnalysisEntity entity) {
        return new TaskAnalysisResponse(
                entity.getId(),
                entity.getTaskId(),
                entity.getComplexityScore(),
                entity.getComplexityLevel(),
                entity.getPredictedEffort(),
                entity.getAnalysisDetails() == null
                        ? Map.of()
                        : entity.getAnalysisDetails(),
                entity.getModelVersion()
        );
    }

    public RiskFactorResponse toRiskFactorResponse(RiskFactorEntity entity) {
        return new RiskFactorResponse(
                entity.getId(),
                entity.getFactorType(),
                entity.getFactorValue(),
                entity.getImpactScore(),
                entity.getDescription()
        );
    }

    public List<RiskFactorResponse> toRiskFactorResponseList(
            List<RiskFactorEntity> entities
    ) {
        return entities.stream()
                .map(this::toRiskFactorResponse)
                .toList();
    }

    public ProjectRiskAssessmentResponse toProjectRiskAssessmentResponse(
            ProjectRiskAssessmentEntity entity
    ) {
        Map<String, Object> details = entity.getAssessmentDetails() == null
                ? Map.of()
                : entity.getAssessmentDetails();

        return new ProjectRiskAssessmentResponse(
                entity.getId(),
                entity.getProjectId(),
                entity.getRiskScore(),
                entity.getRiskLevel(),
                entity.getDelayProbability(),
                entity.getPredictedCompletionDate(),
                toRiskFactorResponseList(entity.getRiskFactors()),
                getRecommendations(details),
                getConfidence(details),
                details,
                entity.getAssessedAt(),
                entity.getModelVersion()
        );
    }

    public List<ProjectRiskAssessmentResponse> toProjectRiskAssessmentResponseList(
            List<ProjectRiskAssessmentEntity> entities
    ) {
        return entities.stream()
                .map(this::toProjectRiskAssessmentResponse)
                .toList();
    }

    public RiskAssessmentHistoryResponse toRiskAssessmentHistoryResponse(
            java.util.UUID projectId,
            List<ProjectRiskAssessmentEntity> entities
    ) {
        return new RiskAssessmentHistoryResponse(
                projectId,
                entities.size(),
                toProjectRiskAssessmentResponseList(entities)
        );
    }

    public RecommendationResponse toRecommendationResponse(
            ProjectRiskAssessmentEntity entity
    ) {
        Map<String, Object> details = entity.getAssessmentDetails() == null
                ? Map.of()
                : entity.getAssessmentDetails();

        return new RecommendationResponse(
                entity.getProjectId(),
                entity.getId(),
                entity.getRiskLevel(),
                getRecommendations(details),
                entity.getAssessedAt()
        );
    }

    private List<String> getRecommendations(Map<String, Object> details) {
        Object value = details.get("recommendations");

        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private Double getConfidence(Map<String, Object> details) {
        Object value = details.get("confidence");

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }
}
