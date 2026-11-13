package com.projectmanagement.communication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на редактирование комментария")
public record CommentUpdateRequest(

        @Schema(
                description = "Новый текст комментария",
                example = "Задача выполнена, изменения отправлены на проверку",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Comment body must not be blank")
        @Size(max = 5000, message = "Comment body must not exceed 5000 characters")
        String body
) {
}