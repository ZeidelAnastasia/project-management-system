package com.projectmanagement.intelligence.risk.model;

public record ProjectRiskFactor(
        String factorType,
        double factorValue,
        double impactScore,
        String description
) {}