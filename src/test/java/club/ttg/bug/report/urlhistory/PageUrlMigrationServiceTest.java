package club.ttg.bug.report.urlhistory;

import club.ttg.bug.report.repository.BugReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PageUrlMigrationServiceTest {

    private static final Instant T = Instant.parse("2026-11-14T10:00:00Z");

    private CoreApiUrlHistoryClient client;
    private PageUrlHistoryStore store;
    private BugReportRepository repository;
    private PageUrlMigrationService service;

    @BeforeEach
    void setUp() {
        client = mock(CoreApiUrlHistoryClient.class);
        store = mock(PageUrlHistoryStore.class);
        repository = mock(BugReportRepository.class);
        service = new PageUrlMigrationService(client, store, repository, Duration.ofHours(1));
    }

    private static BugReportRepository.BugReportUrl report(UUID id, String url) {
        return new BugReportRepository.BugReportUrl() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public String getUrl() {
                return url;
            }
        };
    }

    @Test
    void firstSyncFetchesEverything() {
        when(store.lastChangedAt()).thenReturn(Optional.empty());
        when(client.fetch(null)).thenReturn(List.of());

        assertThat(service.sync()).isZero();

        verify(client).fetch(null);
    }

    @Test
    void nextSyncFetchesSinceCursorWithOverlap() {
        when(store.lastChangedAt()).thenReturn(Optional.of(T));
        when(client.fetch(any())).thenReturn(List.of());

        service.sync();

        verify(client).fetch(T.minus(Duration.ofHours(1)));
    }

    @Test
    void movesReportsToCurrentUrl() {
        UUID onOld = UUID.randomUUID();
        UUID onMiddle = UUID.randomUUID();
        UUID onCurrent = UUID.randomUUID();
        UUID elsewhere = UUID.randomUUID();
        var entries = List.of(
                new UrlHistoryEntry("spells", "a-phb", "a-ph", T),
                new UrlHistoryEntry("spells", "a-old", "a-ph", T));
        when(store.lastChangedAt()).thenReturn(Optional.empty());
        when(client.fetch(null)).thenReturn(entries);
        when(store.apply(any())).thenReturn(true);
        when(store.loadAll()).thenReturn(Map.of("spells", Map.of("a-phb", "a-ph", "a-old", "a-ph")));
        when(repository.findAllWithUrl()).thenReturn(List.of(
                report(onOld, "/spells/a-old?x=1"),
                report(onMiddle, "/spells/a-phb"),
                report(onCurrent, "/spells/a-ph"),
                report(elsewhere, "/bestiary/a-phb")));

        assertThat(service.sync()).isEqualTo(2);

        verify(repository).updateUrl(onOld, "/spells/a-ph?x=1");
        verify(repository).updateUrl(onMiddle, "/spells/a-ph");
        verify(repository).findAllWithUrl();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void repeatedSyncChangesNothing() {
        when(store.lastChangedAt()).thenReturn(Optional.of(T));
        when(client.fetch(any())).thenReturn(List.of(new UrlHistoryEntry("spells", "a-phb", "a-ph", T)));
        when(store.apply(any())).thenReturn(false);

        assertThat(service.sync()).isZero();

        verify(repository, never()).findAllWithUrl();
        verify(repository, never()).updateUrl(any(), anyString());
    }

    @Test
    void currentUrlUsesStore() {
        when(store.findNewSlug("spells", "a-phb")).thenReturn(Optional.of("a-ph"));
        when(store.findNewSlug("spells", "a-ph")).thenReturn(Optional.empty());

        assertThat(service.currentUrl("/spells/a-phb")).isEqualTo("/spells/a-ph");
        assertThat(service.currentUrl(null)).isNull();
    }
}
