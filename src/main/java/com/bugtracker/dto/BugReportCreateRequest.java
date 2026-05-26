package com.bugtracker.dto;

import com.bugtracker.model.SourcePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для создания нового баг-репорта.
 */
@Data
public class BugReportCreateRequest {

    /**
     * Описание бага.
     */
    @NotBlank(message = "Описание бага не может быть пустым")
    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    private String description;

    /**
     * URL страницы, на которой обнаружен баг.
     */
    @Size(max = 1000, message = "URL не должен превышать 1000 символов")
    private String url;

    /**
     * Платформа-источник бага.
     */
    @NotNull(message = "Платформа-источник обязательна")
    private SourcePlatform sourcePlatform;

    /**
     * Логин пользователя (если авторизован).
     */
    private String userLogin;

    /**
     * Идентификатор сессии (если не авторизован).
     */
    private String sessionId;
}
