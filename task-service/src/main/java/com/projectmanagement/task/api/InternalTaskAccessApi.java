package com.projectmanagement.task.api;

import com.projectmanagement.task.client.dto.TaskAccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Internal Task Access", description = "Внутренние операции проверки доступа к задачам")
@RequestMapping("/internal/tasks")
public interface InternalTaskAccessApi {

    @Operation(
            summary = "Получить информацию о доступе к задаче",
            description = "Используется другими микросервисами для проверки прав пользователя на задачу"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о доступе получена",
                    content = @Content(schema = @Schema(implementation = TaskAccessResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Переданный userId не соответствует JWT")
    })
    @GetMapping("/{taskId}/access")
    TaskAccessResponse getTaskAccess(
            @Parameter(description = "Идентификатор задачи")
            @PathVariable UUID taskId,

            @Parameter(description = "Идентификатор пользователя")
            @RequestParam UUID userId
    );
}