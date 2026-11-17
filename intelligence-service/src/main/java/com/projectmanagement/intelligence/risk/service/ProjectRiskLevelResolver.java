package com.projectmanagement.intelligence.risk.service;

import org.springframework.stereotype.Service;

@Service
public class ProjectRiskLevelResolver {

    public String resolve(double riskScore) {
        if (riskScore < 35) {
            return "LOW";
        }

        if (riskScore < 70) {
            return "MEDIUM";
        }

        return "HIGH";
    }
}