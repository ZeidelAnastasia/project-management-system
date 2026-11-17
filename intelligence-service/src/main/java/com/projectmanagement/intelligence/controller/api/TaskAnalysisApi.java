package com.projectmanagement.intelligence.controller.api;

import com.projectmanagement.intelligence.dto.request.TaskAnalysisRequest;
import com.projectmanagement.intelligence.dto.response.TaskAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/intelligence/tasks")
@Tag(name = "Task Analysis", description = "Интеллектуальный анализ задач")
public interface TaskAnalysisApi {

    @GetMapping("/{taskId}/analysis")
    @Operation(
            summary = "Получить анализ задачи",
            description = "Возвращает сложность задачи и прогнозируемые трудозатраты"
    )
    @ApiResponse(responseCode = "200", description = "Результат анализа получен")
    @ApiResponse(responseCode = "404", description = "Результат анализа не найден")
    ResponseEntity<TaskAnalysisResponse> getAnalysis(
            @Parameter(description = "Идентификатор задачи")
            @PathVariable UUID taskId
    );

    @PostMapping("/{taskId}/analysis")
    @Operation(
            summary = "Запустить анализ задачи",
            description = "Рассчитывает сложность задачи и прогнозируемые трудозатраты"
    )
    @ApiResponse(responseCode = "200", description = "Анализ выполнен")
    @ApiResponse(responseCode = "400", description = "Некорректные входные данные")
    @ApiResponse(responseCode = "404", description = "Снимок задачи не найден")
    ResponseEntity<TaskAnalysisResponse> analyze(
            @Parameter(description = "Идентификатор задачи")
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskAnalysisRequest request
    );
}