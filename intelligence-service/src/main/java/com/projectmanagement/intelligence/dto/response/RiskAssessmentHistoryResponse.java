package com.projectmanagement.intelligence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "История оценок риска проекта")
public record RiskAssessmentHistoryResponse(

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Количество выполненных оценок")
        Integer totalAssessments,

        @Schema(description = "История оценок риска")
        List<ProjectRiskAssessmentResponse> assessments
) {
}