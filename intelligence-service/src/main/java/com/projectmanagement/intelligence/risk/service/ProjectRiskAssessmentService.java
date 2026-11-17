package com.projectmanagement.intelligence.risk.service;

import com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity;

import java.util.List;
import java.util.UUID;

public interface ProjectRiskAssessmentService {

    ProjectRiskAssessmentEntity assess(UUID projectId);

    ProjectRiskAssessmentEntity getCurrentAssessment(UUID projectId);

    List<ProjectRiskAssessmentEntity> getAssessmentHistory(UUID projectId);

    List<String> getRecommendations(UUID projectId);
}