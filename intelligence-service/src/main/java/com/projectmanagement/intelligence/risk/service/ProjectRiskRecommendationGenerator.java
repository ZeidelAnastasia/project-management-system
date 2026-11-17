package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.risk.model.ProjectRiskFactor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProjectRiskRecommendationGenerator {

    private static final Map<String, String> RECOMMENDATIONS = Map.of(
            "OVERDUE_TASKS",
            "Проверить просроченные задачи и определить причины задержек.",
            "UNFINISHED_TASKS",
            "Пересмотреть план оставшихся работ и при необходимости скорректировать сроки проекта.",
            "HIGH_PRIORITY_TASKS",
            "Уделить первоочередное внимание незавершённым задачам с высоким приоритетом.",
            "HIGH_COMPLEXITY_TASKS",
            "Разбить сложные задачи на более мелкие этапы и пересмотреть оценку трудозатрат.",
            "ASSIGNEE_OVERLOAD",
            "Перераспределить задачи между сотрудниками для снижения перегрузки.",
            "TASK_REASSIGNMENTS",
            "Проанализировать причины частого переназначения задач.",
            "DEADLINE_CHANGES",
            "Проверить задачи, сроки которых неоднократно изменялись.",
            "SCHEDULE_PRESSURE",
            "Пересмотреть календарный план проекта с учётом прогнозируемой даты завершения."
    );

    public List<String> generate(List<ProjectRiskFactor> factors) {
        List<String> recommendations = new ArrayList<>();

        for (ProjectRiskFactor factor : factors) {
            if (factor.impactScore() >= 3) {
                String recommendation = RECOMMENDATIONS.get(
                        factor.factorType()
                );

                if (recommendation != null) {
                    recommendations.add(recommendation);
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "Продолжать контролировать сроки выполнения задач и загрузку сотрудников."
            );
        }

        return recommendations;
    }
}