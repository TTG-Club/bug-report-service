package club.ttg.bug.report.urlhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Локальная копия прежних адресов записей каталога (таблица {@code page_url_history}).
 */
@Repository
@RequiredArgsConstructor
public class PageUrlHistoryStore {

    private final JdbcTemplate jdbc;

    /** Текущий адрес записи, если {@code slug} в разделе числится прежним. */
    public Optional<String> findNewSlug(String section, String slug) {
        List<String> found = jdbc.queryForList(
                "SELECT new_slug FROM page_url_history WHERE section = ? AND old_slug = ?",
                String.class, section, slug);
        return found.stream().findFirst();
    }

    /** Все пары: раздел → (прежний адрес → текущий). */
    public Map<String, Map<String, String>> loadAll() {
        Map<String, Map<String, String>> result = new HashMap<>();
        jdbc.query("SELECT section, old_slug, new_slug FROM page_url_history", row -> {
            result.computeIfAbsent(row.getString(1), key -> new HashMap<>())
                    .put(row.getString(2), row.getString(3));
        });
        return result;
    }

    /** Самая поздняя смена адреса, которую уже знаем, — курсор синхронизации. */
    public Optional<Instant> lastChangedAt() {
        Timestamp last = jdbc.queryForObject("SELECT MAX(changed_at) FROM page_url_history", Timestamp.class);
        return Optional.ofNullable(last).map(Timestamp::toInstant);
    }

    /**
     * Запоминает смену адреса. Идемпотентно: повтор той же пары ничего не меняет.
     *
     * @return {@code true}, если что-то изменилось
     */
    public boolean apply(UrlHistoryEntry entry) {
        String section = entry.section();
        String oldSlug = entry.oldUrl();
        String newSlug = entry.newUrl();
        if (section == null || oldSlug == null || newSlug == null || oldSlug.equals(newSlug)) {
            return false;
        }
        if (findNewSlug(section, oldSlug).filter(newSlug::equals).isPresent()) {
            return false;
        }

        // Текущий адрес не может числиться прежним (запись вернули на старый адрес: A→B, затем B→A).
        jdbc.update("DELETE FROM page_url_history WHERE section = ? AND old_slug = ?", section, newSlug);
        jdbc.update("""
                        INSERT INTO page_url_history (section, old_slug, new_slug, changed_at)
                        VALUES (?, ?, ?, ?)
                        ON CONFLICT (section, old_slug)
                        DO UPDATE SET new_slug = EXCLUDED.new_slug, changed_at = EXCLUDED.changed_at
                        """,
                section, oldSlug, newSlug, Timestamp.from(changedAt(entry)));
        // Цепочка: всё, что вело на прежний адрес, теперь ведёт на текущий (X→A и A→B дают X→B).
        jdbc.update("UPDATE page_url_history SET new_slug = ? WHERE section = ? AND new_slug = ?",
                newSlug, section, oldSlug);
        return true;
    }

    private static Instant changedAt(UrlHistoryEntry entry) {
        return entry.changedAt() == null ? Instant.now() : entry.changedAt();
    }
}
