package com.bugtracker.dto;

import com.bugtracker.model.BugStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для обновления статуса баг-репорта.
 */
@Data
public class BugReportUpdateStatusRequest {

    /**
     * Новый статус бага.
     */
    @NotNull(message = "Статус обязателен")
    private BugStatus status;

    /**
     * Комментарий при изменении статуса.
     */
    @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
    private String comment;
}
