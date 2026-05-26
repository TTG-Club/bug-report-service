package club.ttg.bug.report.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BugStatus {
    NEW("новый"),
    WAIT("ожидает"),
    FIXED("исправлено"),
    REJECTED("отклонено");

    private final String name;
}
