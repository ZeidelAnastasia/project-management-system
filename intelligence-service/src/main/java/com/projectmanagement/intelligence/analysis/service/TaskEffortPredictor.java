package com.projectmanagement.intelligence.analysis.service;

import com.projectmanagement.intelligence.analysis.model.TaskFeatures;

public interface TaskEffortPredictor {

    double predict(TaskFeatures features, double complexityScore);
}