package club.ttg.bug.report.service.impl;

import club.ttg.bug.report.exception.BugReportNotFoundException;
import club.ttg.bug.report.exception.BugReportNotWithdrawableException;
import club.ttg.bug.report.exception.FileStorageException;
import club.ttg.bug.report.mapper.BugReportMapper;
import club.ttg.bug.report.model.BugReport;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.repository.BugReportRepository;
import club.ttg.bug.report.service.FileStorageService;
import club.ttg.bug.report.urlhistory.PageUrlMigrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Отзыв баг-репорта автором: только своя запись и только в статусе NEW.
 * Транзакцию имитируем через TransactionSynchronizationManager, чтобы проверить,
 * что скриншот удаляется только после коммита.
 */
class BugReportWithdrawTest {

    private static final String AUTHOR = "author";
    private static final String SCREENSHOT = "screenshots/shot.webp";

    private BugReportRepository repository;
    private FileStorageService fileStorageService;
    private BugReportServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(BugReportRepository.class);
        fileStorageService = mock(FileStorageService.class);
        service = new BugReportServiceImpl(repository, mock(BugReportMapper.class),
                fileStorageService, mock(PageUrlMigrationService.class));
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private BugReport stored(String userLogin, BugStatus status, String screenshotPath) {
        BugReport bug = BugReport.builder()
                .id(UUID.randomUUID())
                .userLogin(userLogin)
                .status(status)
                .screenshotPath(screenshotPath)
                .build();
        when(repository.findById(bug.getId())).thenReturn(Optional.of(bug));
        return bug;
    }

    private static void commit() {
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
    }

    @Test
    void ownNewReportIsDeletedAndScreenshotRemovedAfterCommit() {
        BugReport bug = stored(AUTHOR, BugStatus.NEW, SCREENSHOT);

        service.withdrawByAuthor(bug.getId(), AUTHOR);

        verify(repository).delete(bug);
        // До коммита файл не трогаем: удаление строки ещё может откатиться.
        verify(fileStorageService, never()).delete(anyString());

        commit();

        verify(fileStorageService).delete(SCREENSHOT);
    }

    @Test
    void ownNewReportWithoutScreenshotDoesNotTouchStorage() {
        BugReport bug = stored(AUTHOR, BugStatus.NEW, null);

        service.withdrawByAuthor(bug.getId(), AUTHOR);
        commit();

        verify(repository).delete(bug);
        verify(fileStorageService, never()).delete(anyString());
    }

    @Test
    void someoneElsesReportIsNotFound() {
        BugReport bug = stored("other", BugStatus.NEW, SCREENSHOT);

        assertThatThrownBy(() -> service.withdrawByAuthor(bug.getId(), AUTHOR))
                .isInstanceOf(BugReportNotFoundException.class);

        verify(repository, never()).delete(any(BugReport.class));
    }

    @Test
    void anonymousReportIsNotFound() {
        BugReport bug = stored(null, BugStatus.NEW, null);

        assertThatThrownBy(() -> service.withdrawByAuthor(bug.getId(), AUTHOR))
                .isInstanceOf(BugReportNotFoundException.class);

        verify(repository, never()).delete(any(BugReport.class));
    }

    @Test
    void missingReportIsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdrawByAuthor(id, AUTHOR))
                .isInstanceOf(BugReportNotFoundException.class);

        verify(repository, never()).delete(any(BugReport.class));
    }

    @ParameterizedTest
    @EnumSource(value = BugStatus.class, names = "NEW", mode = EnumSource.Mode.EXCLUDE)
    void reportInWorkCannotBeWithdrawn(BugStatus status) {
        BugReport bug = stored(AUTHOR, status, SCREENSHOT);

        assertThatThrownBy(() -> service.withdrawByAuthor(bug.getId(), AUTHOR))
                .isInstanceOf(BugReportNotWithdrawableException.class)
                .hasMessage("Баг-репорт уже взят в работу, отозвать его нельзя");

        commit();

        verify(repository, never()).delete(any(BugReport.class));
        verify(fileStorageService, never()).delete(anyString());
    }

    @Test
    void storageFailureAfterCommitIsOnlyLogged() {
        BugReport bug = stored(AUTHOR, BugStatus.NEW, SCREENSHOT);
        doThrow(new FileStorageException("S3 недоступен")).when(fileStorageService).delete(SCREENSHOT);

        service.withdrawByAuthor(bug.getId(), AUTHOR);

        assertThatCode(BugReportWithdrawTest::commit).doesNotThrowAnyException();
        verify(fileStorageService).delete(SCREENSHOT);
    }
}
