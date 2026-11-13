package com.projectmanagement.communication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание комментария к задаче")
public record CommentCreateRequest(

        @Schema(
                description = "Текст комментария",
                example = "Задача выполнена, можно проверять",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Comment body must not be blank")
        @Size(max = 5000, message = "Comment body must not exceed 5000 characters")
        String body
) {
}