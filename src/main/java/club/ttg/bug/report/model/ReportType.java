package club.ttg.bug.report.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тип записи: баг или пожелание.
 * Пожелания не учитываются в публичной статистике.
 */
@AllArgsConstructor
@Getter
public enum ReportType {
    BUG("баг"),
    WISH("пожелание");

    private final String name;
}
