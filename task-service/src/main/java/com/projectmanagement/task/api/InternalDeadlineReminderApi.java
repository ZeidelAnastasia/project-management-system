package com.projectmanagement.task.api;

import com.projectmanagement.task.dto.response.DeadlineReminderTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Internal Deadline Reminders", description = "Внутренние операции для notification-service")
@RequestMapping("/api/internal/tasks")
public interface InternalDeadlineReminderApi {

    @Operation(
            summary = "Получить задачи для напоминаний о сроках",
            description = "Возвращает задачи с назначенными исполнителями, срок которых попадает в указанный интервал, кроме завершённых задач"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список задач получен",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = DeadlineReminderTaskResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный интервал дат"),
            @ApiResponse(responseCode = "401", description = "Отсутствует или неверный внутренний токен")
    })
    @GetMapping("/deadline-reminders")
    List<DeadlineReminderTaskResponse> getDeadlineReminderTasks(
            @Parameter(description = "Начало интервала в ISO-8601")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @Parameter(description = "Конец интервала в ISO-8601")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to
    );
}