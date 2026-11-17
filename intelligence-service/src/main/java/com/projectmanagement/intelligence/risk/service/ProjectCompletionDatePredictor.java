package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.risk.model.ProjectRiskMetrics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProjectCompletionDatePredictor {

    public LocalDate predict(ProjectRiskMetrics metrics) {
        if (metrics.unfinishedTasks() == 0) {
            return LocalDate.now();
        }

        if (metrics.tasksPerDay() <= 0) {
            return null;
        }

        long remainingDays = (long) Math.ceil(
                metrics.unfinishedTasks() / metrics.tasksPerDay()
        );

        return LocalDate.now().plusDays(Math.max(1, remainingDays));
    }
}