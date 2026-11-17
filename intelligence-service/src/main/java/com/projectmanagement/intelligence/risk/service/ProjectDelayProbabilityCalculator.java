package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.risk.model.ProjectRiskMetrics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ProjectDelayProbabilityCalculator {

    public double calculate(
            ProjectRiskMetrics metrics,
            LocalDate predictedCompletionDate,
            double riskScore
    ) {
        if (metrics.unfinishedTasks() == 0) {
            return 0;
        }

        if (predictedCompletionDate == null) {
            return 0.75;
        }

        if (metrics.plannedEndDate() == null) {
            return Math.min(0.75, riskScore / 100);
        }

        long delayDays = ChronoUnit.DAYS.between(
                metrics.plannedEndDate(),
                predictedCompletionDate
        );

        if (delayDays <= 0) {
            return Math.min(0.45, riskScore / 200);
        }

        long availableDays = Math.max(
                1,
                ChronoUnit.DAYS.between(LocalDate.now(), metrics.plannedEndDate())
        );

        double delayRatio = (double) delayDays / availableDays;

        return Math.min(
                0.98,
                Math.max(0, 0.25 + delayRatio * 0.55 + riskScore / 500)
        );
    }
}