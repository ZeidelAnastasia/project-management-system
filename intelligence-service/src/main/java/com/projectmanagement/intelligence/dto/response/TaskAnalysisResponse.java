package com.projectmanagement.intelligence.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Результат анализа сложности задачи")
public record TaskAnalysisResponse(

        @Schema(description = "Идентификатор результата анализа")
        UUID id,

        @Schema(description = "Идентификатор задачи")
        UUID taskId,

        @Schema(description = "Числовая оценка сложности", example = "75.5")
        Double complexityScore,

        @Schema(
                description = "Категория сложности",
                allowableValues = {"LOW", "MEDIUM", "HIGH"},
                example = "HIGH"
        )
        String complexityLevel,

        @Schema(description = "Прогнозируемые трудозатраты в часах", example = "24.5")
        Double predictedEffort,

        @Schema(description = "Дополнительные характеристики, использованные при анализе")
        Map<String, Object> analysisDetails,

        @Schema(description = "Версия модели", example = "1.0.0")
        String modelVersion
) {
}