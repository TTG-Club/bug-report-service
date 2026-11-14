package club.ttg.bug.report.urlhistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Клиент внутренней ручки core-api {@code GET /api/internal/url-history}:
 * прежние адреса записей каталога. Защита — общий секрет сервисов в заголовке
 * {@code X-Service-Token}.
 */
@Component
public class CoreApiUrlHistoryClient {
    private static final String PATH = "/api/internal/url-history";
    private static final String TOKEN_HEADER = "X-Service-Token";

    private final RestClient restClient;
    private final String serviceSecret;

    public CoreApiUrlHistoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${core-api.base-url}") String coreApiBaseUrl,
            @Value("${core-api.connect-timeout:2s}") Duration connectTimeout,
            @Value("${core-api.read-timeout:30s}") Duration readTimeout,
            @Value("${internal.service-secret:}") String serviceSecret
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restClient = restClientBuilder
                .baseUrl(coreApiBaseUrl)
                .requestFactory(factory)
                .build();
        this.serviceSecret = serviceSecret;
    }

    /** Без секрета ходить в core-api бессмысленно — синхронизация выключена. */
    public boolean isConfigured() {
        return StringUtils.hasText(serviceSecret);
    }

    /**
     * @param since вернуть только смены после этого момента; {@code null} — все пары
     */
    public List<UrlHistoryEntry> fetch(Instant since) {
        List<UrlHistoryEntry> entries = restClient.get()
                .uri(builder -> builder.path(PATH)
                        .queryParamIfPresent("since", Optional.ofNullable(since).map(Instant::toString))
                        .build())
                .header(TOKEN_HEADER, serviceSecret)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UrlHistoryEntry>>() {
                });
        return entries == null ? List.of() : entries;
    }
}
