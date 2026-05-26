package club.ttg.bug.report.dto;

import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO ответа с информацией о баг-репорте.
 */
@Data
@Schema(description = "Информация о баг-репорте")
public class BugReportResponse {

    /**
     * Уникальный идентификатор бага.
     */
    @Schema(description = "Уникальный идентификатор бага", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    /**
     * Описание бага.
     */
    @Schema(description = "Описание бага", example = "Кнопка 'Сохранить' не работает на странице персонажа")
    private String description;

    /**
     * URL страницы, на которой обнаружен баг.
     */
    @Schema(description = "URL страницы, на которой обнаружен баг", example = "https://ttg.club/characters/123")
    private String url;

    /**
     * Текущий статус бага.
     */
    @Schema(description = "Текущий статус бага", example = "NEW")
    private BugStatus status;

    /**
     * Платформа-источник.
     */
    @Schema(description = "Платформа-источник", example = "SITE_5E24")
    private SourcePlatform sourcePlatform;

    /**
     * URL скриншота.
     */
    @Schema(description = "URL скриншота в S3-хранилище", example = "https://s3.ru1.storage.beget.cloud/bucket/screenshots/abc123.png")
    private String screenshotUrl;

    /**
     * Логин пользователя.
     */
    @Schema(description = "Логин пользователя", example = "user123")
    private String userLogin;

    /**
     * Идентификатор сессии.
     */
    @Schema(description = "Идентификатор сессии", example = "sess_abc123def456")
    private String sessionId;

    /**
     * Дата создания баг-репорта.
     */
    @Schema(description = "Дата создания баг-репорта", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения статуса.
     */
    @Schema(description = "Дата последнего изменения статуса", example = "2025-01-16T14:00:00")
    private LocalDateTime statusUpdatedAt;

    /**
     * Комментарий при изменении статуса.
     */
    @Schema(description = "Комментарий при последнем изменении статуса", example = "Исправлено в релизе 2.1.0")
    private String statusComment;
}
