package club.ttg.bug.report.urlhistory;

import club.ttg.bug.report.repository.BugReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Перенос адресов страниц в баг-репортах после смены адреса записи каталога в core-api
 * ({@code /spells/fireball-phb} → {@code /spells/fireball-ph}).
 * <p>
 * Забирает из core-api новые смены адресов (курсор — самая поздняя уже известная смена
 * минус запас {@code overlap}, чтобы не пропустить смены из долгих транзакций),
 * складывает их в {@code page_url_history} и переписывает адреса в репортах.
 * Повторный прогон ничего не меняет.
 */
@Slf4j
@Service
public class PageUrlMigrationService {

    private final CoreApiUrlHistoryClient client;
    private final PageUrlHistoryStore store;
    private final BugReportRepository bugReportRepository;
    private final Duration overlap;

    public PageUrlMigrationService(
            CoreApiUrlHistoryClient client,
            PageUrlHistoryStore store,
            BugReportRepository bugReportRepository,
            @Value("${core-api.url-history.overlap:PT1H}") Duration overlap
    ) {
        this.client = client;
        this.store = store;
        this.bugReportRepository = bugReportRepository;
        this.overlap = overlap;
    }

    public boolean isEnabled() {
        return client.isConfigured();
    }

    /**
     * Один прогон синхронизации.
     *
     * @return сколько баг-репортов получили новый адрес
     */
    @Transactional
    public int sync() {
        Instant since = store.lastChangedAt().map(last -> last.minus(overlap)).orElse(null);
        List<UrlHistoryEntry> entries = client.fetch(since);

        int changedPairs = 0;
        for (UrlHistoryEntry entry : entries) {
            if (store.apply(entry)) {
                changedPairs++;
            }
        }
        if (changedPairs == 0) {
            return 0;
        }

        int moved = migrateReports();
        log.info("Синхронизация адресов из core-api: новых пар {}, перенесено репортов {}", changedPairs, moved);
        return moved;
    }

    /**
     * Текущий адрес страницы: если в нём прежний адрес записи — заменяет на текущий.
     * Нужен при создании репорта, если его отправили со старой ссылки.
     */
    public String currentUrl(String url) {
        return PageUrlRewriter.rewrite(url, store::findNewSlug);
    }

    private int migrateReports() {
        Map<String, Map<String, String>> history = store.loadAll();
        int moved = 0;
        for (BugReportRepository.BugReportUrl report : bugReportRepository.findAllWithUrl()) {
            String rewritten = PageUrlRewriter.rewrite(report.getUrl(),
                    (section, slug) -> Optional.ofNullable(history.getOrDefault(section, Map.of()).get(slug)));
            if (!rewritten.equals(report.getUrl())) {
                bugReportRepository.updateUrl(report.getId(), rewritten);
                moved++;
            }
        }
        return moved;
    }
}
