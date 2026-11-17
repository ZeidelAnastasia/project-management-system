package com.projectmanagement.intelligence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Фактор риска проекта")
public record RiskFactorResponse(

        @Schema(description = "Идентификатор фактора")
        UUID id,

        @Schema(description = "Тип фактора", example = "OVERDUE_TASKS")
        String factorType,

        @Schema(description = "Значение фактора", example = "0.35")
        Double factorValue,

        @Schema(description = "Степень влияния на риск", example = "0.8")
        Double impactScore,

        @Schema(
                description = "Описание фактора",
                example = "35% незавершённых задач имеют просроченный срок"
        )
        String description
) {
}