package com.projectmanagement.intelligence.risk.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProjectRiskCalculation(
        double riskScore,
        String riskLevel,
        double delayProbability,
        LocalDate predictedCompletionDate,
        List<ProjectRiskFactor> riskFactors,
        List<String> recommendations
) {}