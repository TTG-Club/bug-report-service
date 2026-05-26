package com.bugtracker.dto;

import com.bugtracker.model.BugStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для обновления статуса баг-репорта.
 */
@Data
@Schema(description = "Запрос на обновление статуса баг-репорта")
public class BugReportUpdateStatusRequest {

    /**
     * Новый статус бага.
     */
    @NotNull(message = "Статус обязателен")
    @Schema(description = "Новый статус бага", example = "FIXED", requiredMode = Schema.RequiredMode.REQUIRED)
    private BugStatus status;

    /**
     * Комментарий при изменении статуса.
     */
    @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
    @Schema(description = "Комментарий при изменении статуса", example = "Исправлено в релизе 2.1.0")
    private String comment;
}
