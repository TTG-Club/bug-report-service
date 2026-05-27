package club.ttg.bug.report.dto;

import club.ttg.bug.report.model.SourcePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для создания нового баг-репорта.
 */
@Data
@Schema(description = "Запрос на создание баг-репорта")
public class BugReportCreateRequest {

    /**
     * Описание бага.
     */
    @NotBlank(message = "Описание бага не может быть пустым")
    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    @Schema(description = "Описание бага", example = "Кнопка 'Сохранить' не работает на странице персонажа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * URL страницы, на которой обнаружен баг.
     */
    @Size(max = 1000, message = "URL не должен превышать 1000 символов")
    @Schema(description = "URL страницы, на которой обнаружен баг", example = "https://ttg.club/characters/123")
    private String url;

    /**
     * Платформа-источник бага.
     */
    @NotNull(message = "Платформа-источник обязательна")
    @Schema(description = "Платформа-источник бага", example = "SITE_5E24", requiredMode = Schema.RequiredMode.REQUIRED)
    private SourcePlatform sourcePlatform;

    /**
     * Логин пользователя (если авторизован).
     */
    @Schema(description = "Логин пользователя (если авторизован)", example = "user123")
    private String userLogin;

    /**
     * Идентификатор сессии (если не авторизован).
     */
    @Schema(description = "Идентификатор сессии (если не авторизован)", example = "sess_abc123def456")
    private String sessionId;

    /**
     * Выделенный текст на странице.
     */
    @Size(max = 5000, message = "Выделенный текст не должен превышать 5000 символов")
    @Schema(description = "Выделенный текст на странице", example = "Текст, который пользователь выделил при создании баг-репорта")
    private String selectedText;
}
