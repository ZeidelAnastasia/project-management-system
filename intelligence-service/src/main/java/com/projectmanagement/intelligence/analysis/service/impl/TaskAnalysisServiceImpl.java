package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.model.TaskAnalysisResult;
import com.projectmanagement.intelligence.analysis.model.TaskFeatures;
import com.projectmanagement.intelligence.analysis.service.*;
import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.dto.response.TaskAnalysisResponse;
import com.projectmanagement.intelligence.entity.TaskAnalysisEntity;
import com.projectmanagement.intelligence.entity.TaskSnapshotEntity;
import com.projectmanagement.intelligence.mapper.IntelligenceMapper;
import com.projectmanagement.intelligence.repository.TaskAnalysisRepository;
import com.projectmanagement.intelligence.repository.TaskSnapshotRepository;
import com.projectmanagement.ml.model.ModelBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAnalysisServiceImpl implements TaskAnalysisService {

    private static final String MODEL_VERSION = "baseline-1.0";

    private final TaskAnalysisRepository taskAnalysisRepository;
    private final TaskSnapshotRepository taskSnapshotRepository;
    private final TaskFeatureExtractor taskFeatureExtractor;
    private final TaskComplexityCalculator taskComplexityCalculator;
    private final MlEffortPredictionService mlEffortPredictionService;
    private final TaskFeaturePreparationService taskFeaturePreparationService;
    private final ModelBundle modelBundle;
    private final IntelligenceMapper intelligenceMapper;

    @Override
    @Transactional
    public TaskAnalysisResponse analyze(UUID taskId, TaskAnalysisRequest request) {
        TaskSnapshotEntity task = taskSnapshotRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Task snapshot not found: " + taskId
                ));

        TaskFeatures features = taskFeatureExtractor.extract(request, task);

        double complexityScore = taskComplexityCalculator.calculate(features);
        String complexityLevel =
                taskComplexityCalculator.determineLevel(complexityScore);

        double[] mlFeatures = taskFeaturePreparationService.prepare(request, task);

        double predictedEffort = mlEffortPredictionService.predict(mlFeatures);

        Map<String, Object> details = Map.ofEntries(
                Map.entry("titleLength", features.titleLength()),
                Map.entry("descriptionLength", features.descriptionLength()),
                Map.entry("labelCount", features.labelCount()),
                Map.entry("priorityWeight", features.priorityWeight()),
                Map.entry("estimatedEffort", features.estimatedEffort()),
                Map.entry("statusChangeCount", features.statusChangeCount()),
                Map.entry("deadlineChangeCount", features.deadlineChangeCount()),
                Map.entry("reassignmentCount", features.reassignmentCount()),
                Map.entry("activeAssigneeTasks", features.activeAssigneeTasks()),
                Map.entry("activeAssigneeEffort", features.activeAssigneeEffort()),
                Map.entry("daysUntilDeadline", features.daysUntilDeadline()),
                Map.entry("averageHistoricalEffort", features.averageHistoricalEffort()),
                Map.entry("similarCompletedTasks", features.similarCompletedTasks())
        );

        TaskAnalysisEntity entity = taskAnalysisRepository.findByTaskId(taskId)
                .orElseGet(TaskAnalysisEntity::new);

        entity.setTaskId(taskId);
        entity.setComplexityScore(complexityScore);
        entity.setComplexityLevel(complexityLevel);
        entity.setPredictedEffort(predictedEffort);
        entity.setAnalysisDetails(details);
        entity.setModelVersion(modelBundle.getModelVersion());

        return intelligenceMapper.toTaskAnalysisResponse(
                taskAnalysisRepository.save(entity)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAnalysisResponse getAnalysis(UUID taskId) {
        return taskAnalysisRepository.findByTaskId(taskId)
                .map(intelligenceMapper::toTaskAnalysisResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Task analysis not found: " + taskId
                ));
    }
}