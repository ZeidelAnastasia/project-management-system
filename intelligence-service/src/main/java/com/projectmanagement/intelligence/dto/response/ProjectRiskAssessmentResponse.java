package com.projectmanagement.intelligence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Результат оценки риска проекта")
public record ProjectRiskAssessmentResponse(

        @Schema(description = "Идентификатор оценки")
        UUID id,

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Числовой балл риска проекта", example = "72.5")
        Double riskScore,

        @Schema(
                description = "Уровень риска проекта",
                allowableValues = {"LOW", "MEDIUM", "HIGH"},
                example = "HIGH"
        )
        String riskLevel,

        @Schema(
                description = "Вероятность нарушения сроков от 0 до 1",
                example = "0.78"
        )
        Double delayProbability,

        @Schema(description = "Предполагаемая дата завершения проекта")
        LocalDate predictedCompletionDate,

        @Schema(description = "Факторы, повлиявшие на оценку риска")
        List<RiskFactorResponse> riskFactors,

        @Schema(description = "Рекомендации по снижению риска")
        List<String> recommendations,

        @Schema(
                description = "Достоверность прогноза от 0 до 1",
                example = "0.85"
        )
        Double confidence,

        @Schema(description = "Дополнительные сведения об оценке")
        Map<String, Object> assessmentDetails,

        @Schema(description = "Дата проведения оценки")
        LocalDateTime assessedAt,

        @Schema(description = "Версия модели", example = "1.0.0")
        String modelVersion
) {
}