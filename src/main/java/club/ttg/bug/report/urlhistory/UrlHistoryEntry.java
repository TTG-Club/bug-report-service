package club.ttg.bug.report.urlhistory;

import java.time.Instant;

/**
 * Смена адреса записи каталога в core-api ({@code GET /api/internal/url-history}).
 * Цепочки на стороне core-api схлопнуты: {@code newUrl} — всегда текущий адрес.
 *
 * @param section   раздел сайта ({@code spells}, {@code bestiary}, …) — первый сегмент пути страницы
 * @param oldUrl    прежний адрес (последний сегмент пути)
 * @param newUrl    текущий адрес (последний сегмент пути)
 * @param changedAt когда адрес сменился
 */
public record UrlHistoryEntry(String section, String oldUrl, String newUrl, Instant changedAt) {
}
