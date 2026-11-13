package com.projectmanagement.communication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Информация о комментарии к задаче")
public record CommentResponse(

        @Schema(description = "Идентификатор комментария")
        UUID id,

        @Schema(description = "Идентификатор задачи")
        UUID taskId,

        @Schema(description = "Идентификатор проекта")
        UUID projectId,

        @Schema(description = "Идентификатор автора комментария")
        UUID authorId,

        @Schema(description = "Текст комментария")
        String body,

        @Schema(description = "Дата и время создания комментария")
        LocalDateTime createdAt,

        @Schema(description = "Дата и время последнего изменения комментария")
        LocalDateTime updatedAt
) {
}