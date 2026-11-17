package com.projectmanagement.intelligence.analysis.model;

public record TaskFeatures(
        int titleLength,
        int descriptionLength,
        int labelCount,
        int priorityWeight,
        int estimatedEffort,
        int statusChangeCount,
        int deadlineChangeCount,
        int reassignmentCount,
        int activeAssigneeTasks,
        int activeAssigneeEffort,
        int daysUntilDeadline,
        double averageHistoricalEffort,
        int similarCompletedTasks
) {
}