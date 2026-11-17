package com.projectmanagement.ml.model;

import lombok.Builder;

import java.util.Map;

@Builder
public record ModelTrainingResult(
        TrainedEffortModel model,
        RegressionMetrics metrics,
        Map<String, Double> featureImportance,
        int trainingSamples,
        int testSamples
) {
}