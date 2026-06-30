package club.ttg.bug.report.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность баг-репорта.
 */
@Entity
@Table(name = "bug_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Описание бага.
     */
    @Column(nullable = false, length = 2000)
    private String description;

    /**
     * URL страницы, на которой обнаружен баг.
     */
    @Column(length = 1000)
    private String url;

    /**
     * Текущий статус бага.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugStatus status;

    /**
     * Платформа-источник.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourcePlatform sourcePlatform;

    /**
     * Путь к скриншоту.
     */
    @Column(name = "screenshot_path")
    private String screenshotPath;

    /**
     * Логин пользователя (если авторизован).
     */
    @Column(name = "user_login")
    private String userLogin;

    /**
     * Идентификатор сессии (если не авторизован).
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * Дата создания баг-репорта (заполняется автоматически).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения статуса.
     */
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    /**
     * Комментарий при изменении статуса.
     */
    @Column(name = "status_comment", length = 2000)
    private String statusComment;

    /**
     * Логин пользователя, который последним изменил статус.
     */
    @Column(name = "status_updated_by")
    private String statusUpdatedBy;

    /**
     * Выделенный текст на странице.
     */
    @Column(name = "selected_text", length = 5000)
    private String selectedText;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = BugStatus.NEW;
        }
    }
}
