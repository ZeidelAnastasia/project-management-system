package com.projectmanagement.intelligence.analysis.service.impl;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;
import com.projectmanagement.intelligence.analysis.service.TaskComplexityCalculator;
import org.springframework.stereotype.Service;

@Service
public class TaskComplexityCalculatorImpl implements TaskComplexityCalculator {

    @Override
    public double calculate(TaskFeatures features) {
        double score = 0;

        score += Math.min(features.titleLength() / 100.0 * 5, 5);
        score += Math.min(features.descriptionLength() / 1000.0 * 15, 15);
        score += Math.min(features.labelCount() * 2.0, 10);
        score += features.priorityWeight() * 7.0;
        score += Math.min(features.estimatedEffort() / 40.0 * 20, 20);
        score += Math.min(features.statusChangeCount() * 2.0, 8);
        score += Math.min(features.deadlineChangeCount() * 3.0, 9);
        score += Math.min(features.reassignmentCount() * 3.0, 9);
        score += Math.min(features.activeAssigneeTasks() * 2.0, 8);
        score += Math.min(features.activeAssigneeEffort() / 40.0 * 8, 8);

        return Math.round(Math.min(score, 100) * 100.0) / 100.0;
    }

    @Override
    public String determineLevel(double complexityScore) {
        if (complexityScore < 35) {
            return "LOW";
        }

        if (complexityScore < 70) {
            return "MEDIUM";
        }

        return "HIGH";
    }
}