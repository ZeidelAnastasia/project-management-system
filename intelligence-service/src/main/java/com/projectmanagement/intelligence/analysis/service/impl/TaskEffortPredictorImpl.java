package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;
import com.projectmanagement.intelligence.analysis.service.TaskEffortPredictor;
import org.springframework.stereotype.Service;

@Service
public class TaskEffortPredictorImpl implements TaskEffortPredictor {

    @Override
    public double predict(TaskFeatures features, double complexityScore) {
        double baseEffort;

        if (features.similarCompletedTasks() >= 3
                && features.averageHistoricalEffort() > 0) {
            baseEffort = features.averageHistoricalEffort();
        } else if (features.estimatedEffort() > 0) {
            baseEffort = features.estimatedEffort();
        } else {
            baseEffort = 4 + complexityScore / 10.0;
        }

        double complexityMultiplier = 0.7 + complexityScore / 100.0;

        double workloadMultiplier = 1.0
                + Math.min(features.activeAssigneeTasks() * 0.03, 0.2)
                + Math.min(features.activeAssigneeEffort() / 200.0, 0.2);

        double historyMultiplier = 1.0
                + Math.min(features.deadlineChangeCount() * 0.04, 0.2)
                + Math.min(features.reassignmentCount() * 0.03, 0.15);

        double predictedEffort = baseEffort
                * complexityMultiplier
                * workloadMultiplier
                * historyMultiplier;

        return Math.round(Math.max(predictedEffort, 1) * 100.0) / 100.0;
    }
}