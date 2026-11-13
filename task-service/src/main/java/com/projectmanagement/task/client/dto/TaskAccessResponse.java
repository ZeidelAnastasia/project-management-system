package com.projectmanagement.task.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Информация о доступе пользователя к задаче")
public record TaskAccessResponse(
        @Schema(description = "Существует ли задача")
        boolean taskExists,

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Идентификатор исполнителя")
        UUID assigneeId,

        @Schema(description = "Является ли пользователь участником проекта")
        boolean projectMember,

        @Schema(description = "Является ли пользователь менеджером проекта")
        boolean projectManager
) {}
