package com.projectmanagement.intelligence.controller.api;

import com.projectmanagement.intelligence.dto.response.ProjectRiskAssessmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/intelligence/projects")
@Tag(name = "Project Risk Assessment", description = "Оценка рисков проектов")
public interface ProjectRiskApi {

    @GetMapping("/{projectId}/risk")
    @Operation(
            summary = "Получить текущую оценку риска проекта",
            description = "Возвращает последнюю оценку риска проекта, вероятность нарушения сроков и факторы риска"
    )
    @ApiResponse(responseCode = "200", description = "Оценка риска получена")
    @ApiResponse(responseCode = "404", description = "Оценка риска не найдена")
    ResponseEntity<ProjectRiskAssessmentResponse> getCurrentAssessment(
            @Parameter(description = "Идентификатор проекта")
            @PathVariable UUID projectId
    );

    @PostMapping("/{projectId}/risk-assessment")
    @Operation(
            summary = "Запустить оценку риска проекта",
            description = "Рассчитывает риск проекта на основе характеристик задач, сроков и загрузки исполнителей"
    )
    @ApiResponse(responseCode = "200", description = "Оценка риска выполнена")
    @ApiResponse(responseCode = "404", description = "Данные проекта не найдены")
    ResponseEntity<ProjectRiskAssessmentResponse> assess(
            @Parameter(description = "Идентификатор проекта")
            @PathVariable UUID projectId
    );

    @GetMapping("/{projectId}/risk-history")
    @Operation(
            summary = "Получить историю оценок риска",
            description = "Возвращает историю ранее выполненных оценок риска проекта"
    )
    @ApiResponse(responseCode = "200", description = "История оценок получена")
    ResponseEntity<List<ProjectRiskAssessmentResponse>> getAssessmentHistory(
            @Parameter(description = "Идентификатор проекта")
            @PathVariable UUID projectId
    );

    @GetMapping("/{projectId}/recommendations")
    @Operation(
            summary = "Получить рекомендации по проекту",
            description = "Возвращает рекомендации по снижению риска нарушения сроков выполнения проекта"
    )
    @ApiResponse(responseCode = "200", description = "Рекомендации получены")
    ResponseEntity<List<String>> getRecommendations(
            @Parameter(description = "Идентификатор проекта")
            @PathVariable UUID projectId
    );
}