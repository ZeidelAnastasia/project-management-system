package com.projectmanagement.intelligence.risk.service.impl;

import com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity;
import com.projectmanagement.intelligence.entity.ProjectSnapshotEntity;
import com.projectmanagement.intelligence.entity.RiskFactorEntity;
import com.projectmanagement.intelligence.repository.ProjectRiskAssessmentRepository;
import com.projectmanagement.intelligence.repository.ProjectSnapshotRepository;
import com.projectmanagement.intelligence.risk.model.ProjectRiskFactor;
import com.projectmanagement.intelligence.risk.model.ProjectRiskMetrics;
import com.projectmanagement.intelligence.risk.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectRiskAssessmentServiceImpl
        implements ProjectRiskAssessmentService {

    private static final String MODEL_VERSION = "heuristic-1.0";

    private final ProjectSnapshotRepository projectSnapshotRepository;
    private final ProjectRiskAssessmentRepository projectRiskAssessmentRepository;
    private final ProjectRiskFeatureExtractor featureExtractor;
    private final ProjectCompletionDatePredictor completionDatePredictor;
    private final ProjectRiskScoreCalculator scoreCalculator;
    private final ProjectRiskLevelResolver riskLevelResolver;
    private final ProjectDelayProbabilityCalculator delayProbabilityCalculator;
    private final ProjectRiskRecommendationGenerator recommendationGenerator;

    @Override
    @Transactional
    public ProjectRiskAssessmentEntity assess(UUID projectId) {
        ProjectSnapshotEntity project = projectSnapshotRepository
                .findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId
                ));

        ProjectRiskMetrics metrics = featureExtractor.extract(
                projectId,
                project.getPlannedEndDate()
        );

        var predictedCompletionDate = completionDatePredictor.predict(metrics);

        List<ProjectRiskFactor> factors = scoreCalculator.calculateFactors(
                metrics,
                predictedCompletionDate
        );

        double riskScore = scoreCalculator.calculateScore(factors);
        String riskLevel = riskLevelResolver.resolve(riskScore);

        double delayProbability = delayProbabilityCalculator.calculate(
                metrics,
                predictedCompletionDate,
                riskScore
        );

        List<String> recommendations = recommendationGenerator.generate(factors);

        ProjectRiskAssessmentEntity assessment =
                ProjectRiskAssessmentEntity.builder()
                        .id(UUID.randomUUID())
                        .projectId(projectId)
                        .riskScore(riskScore)
                        .riskLevel(riskLevel)
                        .delayProbability(delayProbability)
                        .predictedCompletionDate(predictedCompletionDate)
                        .assessmentDetails(buildAssessmentDetails(metrics, recommendations))
                        .assessedAt(LocalDateTime.now())
                        .modelVersion(MODEL_VERSION)
                        .build();

        List<RiskFactorEntity> riskFactorEntities = factors.stream()
                .map(factor -> RiskFactorEntity.builder()
                        .id(UUID.randomUUID())
                        .assessment(assessment)
                        .factorType(factor.factorType())
                        .factorValue(factor.factorValue())
                        .impactScore(factor.impactScore())
                        .description(factor.description())
                        .build())
                .toList();

        assessment.setRiskFactors(new ArrayList<>(riskFactorEntities));

        return projectRiskAssessmentRepository.save(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRiskAssessmentEntity getCurrentAssessment(UUID projectId) {
        return projectRiskAssessmentRepository
                .findFirstByProjectIdOrderByAssessedAtDesc(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Risk assessment not found for project: " + projectId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRiskAssessmentEntity> getAssessmentHistory(UUID projectId) {
        return projectRiskAssessmentRepository
                .findAllByProjectIdOrderByAssessedAtDesc(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRecommendations(UUID projectId) {
        ProjectRiskAssessmentEntity assessment = getCurrentAssessment(projectId);

        Object recommendations = assessment.getAssessmentDetails()
                .get("recommendations");

        if (recommendations instanceof List<?> values) {
            return values.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }

    private Map<String, Object> buildAssessmentDetails(
            ProjectRiskMetrics metrics,
            List<String> recommendations
    ) {
        Map<String, Object> details = new HashMap<>();

        details.put("totalTasks", metrics.totalTasks());
        details.put("completedTasks", metrics.completedTasks());
        details.put("unfinishedTasks", metrics.unfinishedTasks());
        details.put("overdueTasks", metrics.overdueTasks());
        details.put("highPriorityTasks", metrics.highPriorityTasks());
        details.put("highComplexityTasks", metrics.highComplexityTasks());
        details.put("remainingEffort", metrics.remainingEffort());
        details.put("completedEffort", metrics.completedEffort());
        details.put("averageComplexity", metrics.averageComplexity());
        details.put("tasksPerDay", metrics.tasksPerDay());
        details.put("averageTaskDuration", metrics.averageTaskDuration());
        details.put("overloadedAssignees", metrics.overloadedAssignees());
        details.put("activeAssignees", metrics.activeAssignees());
        details.put("reassignmentCount", metrics.reassignmentCount());
        details.put("deadlineChangeCount", metrics.deadlineChangeCount());
        details.put("recommendations", recommendations);

        return details;
    }
}