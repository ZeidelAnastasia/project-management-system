package com.projectmanagement.ml.model;

public record TrainingSample(
        String id,
        String corpus,
        Integer numComment,
        Integer numActivities,
        Double expertEstimatedEffort,
        Double actualEffort,
        String reference
) {
}