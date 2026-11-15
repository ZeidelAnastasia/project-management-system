package com.projectmanagement.reporting.controller.api;

import com.projectmanagement.reporting.dto.response.CompletionDynamicsResponse;
import com.projectmanagement.reporting.dto.response.EmployeeWorkloadResponse;
import com.projectmanagement.reporting.dto.response.OverdueTasksResponse;
import com.projectmanagement.reporting.dto.response.ProjectComparisonResponse;
import com.projectmanagement.reporting.dto.response.ReportSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@Tag(
        name = "Reports",
        description = "Получение статистических отчётов и аналитических показателей"
)
@RequestMapping("/api/reports")
public interface ReportApi {

    @Operation(
            summary = "Получение общей статистики",
            description = "Возвращает сводную статистику по проектам и задачам за указанный период"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика успешно получена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReportSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный период"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/overview")
    ReportSummaryResponse getOverview(
            @Parameter(
                    name = "startDate",
                    description = "Начало периода",
                    in = ParameterIn.QUERY,
                    required = true,
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    name = "endDate",
                    description = "Конец периода",
                    in = ParameterIn.QUERY,
                    required = true,
                    example = "2026-12-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );

    @Operation(
            summary = "Получение отчёта по проекту",
            description = "Возвращает статистические показатели выбранного проекта за указанный период"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчёт успешно получен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReportSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный период"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к проекту")
    })
    @GetMapping("/projects/{projectId}")
    ReportSummaryResponse getProjectReport(
            @Parameter(
                    name = "projectId",
                    description = "Идентификатор проекта",
                    required = true
            )
            @PathVariable UUID projectId,

            @Parameter(
                    name = "startDate",
                    description = "Начало периода",
                    required = true,
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    name = "endDate",
                    description = "Конец периода",
                    required = true,
                    example = "2026-12-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );

    @Operation(
            summary = "Получение количества просроченных задач",
            description = "Возвращает количество просроченных и незавершённых задач проекта на указанную дату"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Количество просроченных задач успешно получено",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OverdueTasksResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к проекту")
    })
    @GetMapping("/projects/{projectId}/overdue-tasks")
    OverdueTasksResponse getOverdueTasks(
            @Parameter(
                    name = "projectId",
                    description = "Идентификатор проекта",
                    required = true
            )
            @PathVariable UUID projectId,

            @Parameter(
                    name = "date",
                    description = "Дата, на которую определяется просрочка",
                    required = true,
                    example = "2026-11-14"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    );

    @Operation(
            summary = "Получение динамики выполнения задач",
            description = "Возвращает ежедневную динамику создания и завершения задач проекта"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Динамика выполнения успешно получена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompletionDynamicsResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный период"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к проекту")
    })
    @GetMapping("/projects/{projectId}/completion-dynamics")
    CompletionDynamicsResponse getCompletionDynamics(
            @Parameter(
                    name = "projectId",
                    description = "Идентификатор проекта",
                    required = true
            )
            @PathVariable UUID projectId,

            @Parameter(
                    name = "startDate",
                    description = "Начало периода",
                    required = true,
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    name = "endDate",
                    description = "Конец периода",
                    required = true,
                    example = "2026-12-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );

    @Operation(
            summary = "Получение загрузки сотрудников",
            description = "Возвращает показатели распределения задач между сотрудниками проекта"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные о загрузке сотрудников успешно получены",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EmployeeWorkloadResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к проекту")
    })
    @GetMapping("/projects/{projectId}/employee-workload")
    EmployeeWorkloadResponse getEmployeeWorkload(
            @Parameter(
                    name = "projectId",
                    description = "Идентификатор проекта",
                    required = true
            )
            @PathVariable UUID projectId
    );

    @Operation(
            summary = "Сравнение проектов",
            description = "Возвращает сравнительные показатели проектов за указанный период"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сравнение проектов успешно получено",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProjectComparisonResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректный период"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/projects/comparison")
    ProjectComparisonResponse getProjectComparison(
            @Parameter(
                    name = "startDate",
                    description = "Начало периода",
                    required = true,
                    example = "2026-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    name = "endDate",
                    description = "Конец периода",
                    required = true,
                    example = "2026-12-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );
}