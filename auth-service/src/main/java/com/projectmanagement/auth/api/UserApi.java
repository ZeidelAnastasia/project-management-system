package com.projectmanagement.auth.api;

import com.projectmanagement.auth.dto.request.RoleUpdateRequest;
import com.projectmanagement.auth.dto.request.UserStatusUpdateRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "Управление пользователями")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/users")
public interface UserApi {

    @Operation(summary = "Получение списка пользователей")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей",
                    content = @Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав", content = @Content)
    })
    @GetMapping
    ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    );

    @Operation(summary = "Получение пользователя по идентификатору")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/{id}")
    ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id
    );

    @Operation(summary = "Изменение системной роли пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль успешно изменена",
                    content = @Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь или роль не найдены", content = @Content)
    })
    @PatchMapping("/{id}/role")
    ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequest request
    );

    @Operation(summary = "Изменение статуса учётной записи пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно изменён",
                    content = @Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @PatchMapping("/{id}/status")
    ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request
    );
}