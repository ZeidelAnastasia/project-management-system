package com.projectmanagement.notification.controller.api;

import com.projectmanagement.notification.dto.MarkAllAsReadResponse;
import com.projectmanagement.notification.dto.NotificationPageResponse;
import com.projectmanagement.notification.dto.NotificationResponse;
import com.projectmanagement.notification.dto.UnreadCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(
        name = "Notifications",
        description = "API для работы с уведомлениями текущего пользователя"
)
@RequestMapping(
        value = "/api/notifications",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface NotificationApi {

    @Operation(
            summary = "Получение уведомлений",
            description = "Возвращает список уведомлений текущего пользователя с пагинацией"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Уведомления успешно получены",
                    content = @Content(
                            schema = @Schema(implementation = NotificationPageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @GetMapping
    NotificationPageResponse getNotifications(
            @Parameter(
                    name = "page",
                    description = "Номер страницы, начиная с 0",
                    in = ParameterIn.QUERY,
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @Parameter(
                    name = "size",
                    description = "Количество уведомлений на странице",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    );

    @Operation(
            summary = "Получение непрочитанных уведомлений",
            description = "Возвращает непрочитанные уведомления текущего пользователя с пагинацией"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Непрочитанные уведомления успешно получены",
                    content = @Content(
                            schema = @Schema(implementation = NotificationPageResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @GetMapping("/unread")
    NotificationPageResponse getUnreadNotifications(
            @Parameter(
                    name = "page",
                    description = "Номер страницы, начиная с 0",
                    in = ParameterIn.QUERY,
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @Parameter(
                    name = "size",
                    description = "Количество уведомлений на странице",
                    in = ParameterIn.QUERY,
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size
    );

    @Operation(
            summary = "Получение количества непрочитанных уведомлений",
            description = "Возвращает количество непрочитанных уведомлений текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Количество уведомлений успешно получено",
                    content = @Content(
                            schema = @Schema(implementation = UnreadCountResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @GetMapping("/unread/count")
    UnreadCountResponse getUnreadCount();

    @Operation(
            summary = "Отметка уведомления как прочитанного",
            description = "Отмечает уведомление текущего пользователя как прочитанное"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Уведомление успешно отмечено как прочитанное",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Уведомление не найдено")
    })
    @PatchMapping("/{id}/read")
    NotificationResponse markAsRead(
            @Parameter(
                    name = "id",
                    description = "Идентификатор уведомления",
                    required = true
            )
            @PathVariable UUID id
    );

    @Operation(
            summary = "Отметка всех уведомлений как прочитанных",
            description = "Отмечает все непрочитанные уведомления текущего пользователя как прочитанные"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Уведомления успешно отмечены как прочитанные",
                    content = @Content(
                            schema = @Schema(implementation = MarkAllAsReadResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @PatchMapping("/read-all")
    MarkAllAsReadResponse markAllAsRead();
}