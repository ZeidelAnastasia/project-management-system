package com.projectmanagement.intelligence.controller;

import com.projectmanagement.intelligence.controller.api.ProjectRiskApi;
import com.projectmanagement.intelligence.dto.response.ProjectRiskAssessmentResponse;
import com.projectmanagement.intelligence.mapper.ProjectRiskAssessmentMapper;
import com.projectmanagement.intelligence.risk.service.ProjectRiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectRiskController implements ProjectRiskApi {

    private final ProjectRiskAssessmentService projectRiskAssessmentService;
    private final ProjectRiskAssessmentMapper projectRiskAssessmentMapper;

    @Override
    public ResponseEntity<ProjectRiskAssessmentResponse> getCurrentAssessment(UUID projectId) {
        return ResponseEntity.ok(
                toResponse(
                        projectRiskAssessmentService.getCurrentAssessment(projectId)
                )
        );
    }

    @Override
    public ResponseEntity<ProjectRiskAssessmentResponse> assess(UUID projectId) {
        return ResponseEntity.ok(
                toResponse(
                        projectRiskAssessmentService.assess(projectId)
                )
        );
    }

    @Override
    public ResponseEntity<List<ProjectRiskAssessmentResponse>> getAssessmentHistory(UUID projectId) {
        List<ProjectRiskAssessmentResponse> history = projectRiskAssessmentService
                .getAssessmentHistory(projectId)
                .stream()
                .map(projectRiskAssessment -> {
                    List<String> recommendations = projectRiskAssessmentService
                            .getRecommendations(projectId);

                    return projectRiskAssessmentMapper.toResponse(
                            projectRiskAssessment,
                            recommendations
                    );
                })
                .toList();

        return ResponseEntity.ok(history);
    }

    @Override
    public ResponseEntity<List<String>> getRecommendations(UUID projectId) {
        return ResponseEntity.ok(
                projectRiskAssessmentService.getRecommendations(projectId)
        );
    }

    private ProjectRiskAssessmentResponse toResponse(
            com.projectmanagement.intelligence.entity.ProjectRiskAssessmentEntity entity
    ) {
        return projectRiskAssessmentMapper.toResponse(
                entity,
                projectRiskAssessmentService.getRecommendations(entity.getProjectId())
        );
    }
}