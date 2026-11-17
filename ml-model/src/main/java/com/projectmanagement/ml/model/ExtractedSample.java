package com.projectmanagement.ml.model;

public record ExtractedSample(
        String id,
        double[] features,
        double actualEffort
) {
}