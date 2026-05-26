package com.bugtracker.dto;

import com.bugtracker.model.BugStatus;
import com.bugtracker.model.SourcePlatform;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа с информацией о баг-репорте.
 */
@Data
public class BugReportResponse {

    /**
     * Уникальный идентификатор бага.
     */
    private UUID id;

    /**
     * Описание бага.
     */
    private String description;

    /**
     * URL страницы, на которой обнаружен баг.
     */
    private String url;

    /**
     * Текущий статус бага.
     */
    private BugStatus status;

    /**
     * Платформа-источник.
     */
    private SourcePlatform sourcePlatform;

    /**
     * URL скриншота.
     */
    private String screenshotUrl;

    /**
     * Логин пользователя.
     */
    private String userLogin;

    /**
     * Идентификатор сессии.
     */
    private String sessionId;

    /**
     * Дата создания баг-репорта.
     */
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения статуса.
     */
    private LocalDateTime statusUpdatedAt;

    /**
     * Комментарий при изменении статуса.
     */
    private String statusComment;
}
