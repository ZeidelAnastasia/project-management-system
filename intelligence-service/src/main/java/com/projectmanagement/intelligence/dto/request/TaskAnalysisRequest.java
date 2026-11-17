package com.projectmanagement.intelligence.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Запрос на анализ сложности задачи")
public record TaskAnalysisRequest(

        @NotBlank
        @Size(max = 255)
        @Schema(description = "Название задачи", example = "Реализовать авторизацию пользователей")
        String title,

        @NotBlank
        @Schema(description = "Описание задачи", example = "Реализовать механизм авторизации с использованием JWT")
        String description,

        @NotBlank
        @Schema(description = "Приоритет задачи", example = "HIGH")
        String priority,

        @Schema(description = "Метки задачи", example = "[\"backend\", \"security\"]")
        List<String> labels,

        @Min(1)
        @Schema(description = "Планируемые трудозатраты в часах", example = "16")
        Integer estimatedEffort
) {
}