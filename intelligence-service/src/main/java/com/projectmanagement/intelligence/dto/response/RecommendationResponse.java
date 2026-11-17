package com.projectmanagement.intelligence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Рекомендации по снижению риска проекта")
public record RecommendationResponse(

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Идентификатор оценки риска")
        UUID assessmentId,

        @Schema(description = "Текущий уровень риска", example = "HIGH")
        String riskLevel,

        @Schema(description = "Рекомендации менеджеру проекта")
        List<String> recommendations,

        @Schema(description = "Дата формирования рекомендаций")
        LocalDateTime assessedAt
) {
}