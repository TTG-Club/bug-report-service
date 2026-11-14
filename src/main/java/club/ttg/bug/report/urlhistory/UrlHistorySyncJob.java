package club.ttg.bug.report.urlhistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Периодический перенос адресов страниц из core-api. Сбой (core-api недоступен и т. п.)
 * только пишется в лог — следующий прогон догонит.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UrlHistorySyncJob {

    private final PageUrlMigrationService migrationService;

    @EventListener(ApplicationReadyEvent.class)
    public void warnIfDisabled() {
        if (!migrationService.isEnabled()) {
            log.warn("INTERNAL_SERVICE_SECRET не задан — перенос адресов страниц из core-api выключен");
        }
    }

    @Scheduled(
            initialDelayString = "${core-api.url-history.initial-delay:PT1M}",
            fixedDelayString = "${core-api.url-history.sync-interval:PT10M}")
    public void run() {
        if (!migrationService.isEnabled()) {
            return;
        }
        try {
            migrationService.sync();
        } catch (RuntimeException e) {
            log.warn("Не удалось синхронизировать адреса страниц из core-api: {}", e.toString());
        }
    }
}
