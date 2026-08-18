package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO сводки изменений по баг-репортам пользователя.
 *
 * Нужна фронту для индикатора «есть новости»: сколько репортов автора получили
 * изменение статуса позже отметки последнего просмотра и какова самая свежая
 * дата изменения (её фронт сохраняет как новую отметку).
 */
@Data
@AllArgsConstructor
@Schema(description = "Сводка изменений по баг-репортам пользователя")
public class MyBugUpdatesResponse {

    /**
     * Количество баг-репортов, статус которых менялся позже переданной отметки.
     */
    @Schema(description = "Количество непросмотренных изменений статуса", example = "3")
    private long count;

    /**
     * Самая свежая дата изменения статуса среди баг-репортов пользователя.
     * Пусто, если статус ни одного репорта ещё не меняли.
     */
    @Schema(description = "Самая свежая дата изменения статуса", example = "2025-01-16T14:00:00")
    private LocalDateTime lastStatusUpdatedAt;
}
