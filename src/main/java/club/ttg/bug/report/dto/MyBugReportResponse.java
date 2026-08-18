package club.ttg.bug.report.dto;

import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO баг-репорта для его автора (ручка {@code /my}).
 *
 * Намеренно не содержит {@code statusUpdatedBy}, {@code userLogin} и
 * {@code sessionId}: автор не должен знать, кто именно менял статус и писал
 * комментарий. Дата изменения статуса при этом доступна.
 */
@Data
@Schema(description = "Баг-репорт в личном кабинете автора")
public class MyBugReportResponse {

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
    @Schema(description = "URL скриншота", example = "https://bug-report.api.ttg.club/api/v1/bugs/550e8400-e29b-41d4-a716-446655440000/screenshot")
    private String screenshotUrl;

    /**
     * Дата создания баг-репорта.
     */
    @Schema(description = "Дата создания баг-репорта", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения статуса. Пусто, если статус ещё не меняли.
     */
    @Schema(description = "Дата последнего изменения статуса", example = "2025-01-16T14:00:00")
    private LocalDateTime statusUpdatedAt;

    /**
     * Комментарий команды к последнему изменению статуса.
     */
    @Schema(description = "Комментарий команды к последнему изменению статуса", example = "Исправлено в релизе 2.1.0")
    private String statusComment;

    /**
     * Выделенный текст на странице.
     */
    @Schema(description = "Выделенный текст на странице", example = "Текст, который пользователь выделил при создании баг-репорта")
    private String selectedText;
}
