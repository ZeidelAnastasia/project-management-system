package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.risk.model.ProjectRiskFactor;
import com.projectmanagement.intelligence.risk.model.ProjectRiskMetrics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectRiskScoreCalculator {

    public List<ProjectRiskFactor> calculateFactors(
            ProjectRiskMetrics metrics,
            LocalDate predictedCompletionDate
    ) {
        List<ProjectRiskFactor> factors = new ArrayList<>();

        double overdueRatio = ratio(
                metrics.overdueTasks(),
                metrics.unfinishedTasks()
        );

        double unfinishedRatio = ratio(
                metrics.unfinishedTasks(),
                metrics.totalTasks()
        );

        double highPriorityRatio = ratio(
                metrics.highPriorityTasks(),
                metrics.unfinishedTasks()
        );

        double highComplexityRatio = ratio(
                metrics.highComplexityTasks(),
                metrics.unfinishedTasks()
        );

        double overloadedRatio = ratio(
                metrics.overloadedAssignees(),
                metrics.activeAssignees()
        );

        double reassignmentRatio = normalize(
                metrics.reassignmentCount(),
                metrics.totalTasks() * 0.5
        );

        double deadlineChangeRatio = normalize(
                metrics.deadlineChangeCount(),
                metrics.totalTasks() * 0.5
        );

        double schedulePressure = calculateSchedulePressure(
                metrics.plannedEndDate(),
                predictedCompletionDate
        );

        factors.add(factor(
                "OVERDUE_TASKS",
                overdueRatio,
                25,
                "Доля просроченных незавершённых задач"
        ));

        factors.add(factor(
                "UNFINISHED_TASKS",
                unfinishedRatio,
                10,
                "Доля незавершённых задач проекта"
        ));

        factors.add(factor(
                "HIGH_PRIORITY_TASKS",
                highPriorityRatio,
                10,
                "Доля незавершённых задач с высоким приоритетом"
        ));

        factors.add(factor(
                "HIGH_COMPLEXITY_TASKS",
                highComplexityRatio,
                15,
                "Доля незавершённых задач высокой сложности"
        ));

        factors.add(factor(
                "ASSIGNEE_OVERLOAD",
                overloadedRatio,
                10,
                "Доля перегруженных исполнителей"
        ));

        factors.add(factor(
                "TASK_REASSIGNMENTS",
                reassignmentRatio,
                10,
                "Частота переназначения задач"
        ));

        factors.add(factor(
                "DEADLINE_CHANGES",
                deadlineChangeRatio,
                10,
                "Частота изменения сроков задач"
        ));

        factors.add(factor(
                "SCHEDULE_PRESSURE",
                schedulePressure,
                10,
                "Отклонение прогнозируемой даты от плановой"
        ));

        return factors;
    }

    public double calculateScore(List<ProjectRiskFactor> factors) {
        return Math.min(
                100,
                factors.stream()
                        .mapToDouble(ProjectRiskFactor::impactScore)
                        .sum()
        );
    }

    private ProjectRiskFactor factor(
            String type,
            double value,
            double weight,
            String description
    ) {
        double normalizedValue = clamp(value);

        return new ProjectRiskFactor(
                type,
                normalizedValue,
                normalizedValue * weight,
                description
        );
    }

    private double calculateSchedulePressure(
            LocalDate plannedEndDate,
            LocalDate predictedCompletionDate
    ) {
        if (predictedCompletionDate == null) {
            return 0.7;
        }

        if (plannedEndDate == null) {
            return 0;
        }

        long delayDays = ChronoUnit.DAYS.between(
                plannedEndDate,
                predictedCompletionDate
        );

        if (delayDays <= 0) {
            return 0;
        }

        long availableDays = Math.max(
                7,
                ChronoUnit.DAYS.between(LocalDate.now(), plannedEndDate)
        );

        return clamp((double) delayDays / availableDays);
    }

    private double ratio(int value, int total) {
        if (total <= 0) {
            return 0;
        }

        return (double) value / total;
    }

    private double normalize(double value, double maximum) {
        if (maximum <= 0) {
            return 0;
        }

        return value / maximum;
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}