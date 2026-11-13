package com.projectmanagement.communication.controller.api;

import com.projectmanagement.communication.dto.request.CommentCreateRequest;
import com.projectmanagement.communication.dto.request.CommentUpdateRequest;
import com.projectmanagement.communication.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(
        name = "Comments",
        description = "API для управления комментариями к задачам"
)
@RequestMapping("/api")
public interface CommentApi {

    @Operation(
            summary = "Создание комментария",
            description = "Создаёт комментарий к задаче от имени текущего пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Комментарий успешно создан",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к задаче"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "503", description = "Сервис задач недоступен")
    })
    @PostMapping("/tasks/{taskId}/comments")
    ResponseEntity<CommentResponse> createComment(
            @Parameter(
                    description = "Идентификатор задачи",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable UUID taskId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового комментария",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentCreateRequest.class))
            )
            @Valid @RequestBody CommentCreateRequest request
    );

    @Operation(
            summary = "Получение комментариев",
            description = "Возвращает страницу комментариев к задаче в порядке создания"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Комментарии успешно получены",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к задаче"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "503", description = "Сервис задач недоступен")
    })
    @GetMapping("/tasks/{taskId}/comments")
    ResponseEntity<Page<CommentResponse>> getCommentsByTaskId(
            @Parameter(
                    description = "Идентификатор задачи",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable UUID taskId,

            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable
    );

    @Operation(
            summary = "Редактирование комментария",
            description = "Изменяет текст собственного комментария пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Комментарий успешно изменён",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на редактирование комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий или задача не найдены"),
            @ApiResponse(responseCode = "503", description = "Сервис задач недоступен")
    })
    @PutMapping("/comments/{id}")
    ResponseEntity<CommentResponse> updateComment(
            @Parameter(
                    description = "Идентификатор комментария",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый текст комментария",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommentUpdateRequest.class))
            )
            @Valid @RequestBody CommentUpdateRequest request
    );

    @Operation(
            summary = "Удаление комментария",
            description = "Удаляет собственный комментарий пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Комментарий успешно удалён"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление комментария"),
            @ApiResponse(responseCode = "404", description = "Комментарий или задача не найдены"),
            @ApiResponse(responseCode = "503", description = "Сервис задач недоступен")
    })
    @DeleteMapping("/comments/{id}")
    ResponseEntity<Void> deleteComment(
            @Parameter(
                    description = "Идентификатор комментария",
                    required = true,
                    in = ParameterIn.PATH
            )
            @PathVariable UUID id
    );
}