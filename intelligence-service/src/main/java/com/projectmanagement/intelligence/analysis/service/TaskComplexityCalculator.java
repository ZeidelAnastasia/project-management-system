package com.projectmanagement.intelligence.analysis.service;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;

public interface TaskComplexityCalculator {

    double calculate(TaskFeatures features);

    String determineLevel(double complexityScore);
}