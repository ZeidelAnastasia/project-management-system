package com.projectmanagement.ml.model;

public record PreparedSample(
        String id,
        String corpus,
        String cleanedText,
        Integer numComment,
        Integer numActivities,
        Double expertEstimatedEffort,
        double actualEffort,
        String reference
) {
}