package club.ttg.bug.report.repository;

import club.ttg.bug.report.model.BugReport;
import club.ttg.bug.report.model.BugStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с баг-репортами.
 */
@Repository
public interface BugReportRepository extends JpaRepository<BugReport, UUID>, JpaSpecificationExecutor<BugReport> {

    /**
     * Логины авторов баг-репортов по алфавиту. Анонимные репорты (без логина) не учитываются.
     */
    @Query("SELECT DISTINCT b.userLogin FROM BugReport b WHERE b.userLogin IS NOT NULL ORDER BY b.userLogin")
    List<String> findDistinctUserLogins();

    /**
     * Логины пользователей, менявших статус баг-репортов, по алфавиту.
     */
    @Query("SELECT DISTINCT b.statusUpdatedBy FROM BugReport b WHERE b.statusUpdatedBy IS NOT NULL ORDER BY b.statusUpdatedBy")
    List<String> findDistinctStatusUpdatedBy();

    /**
     * Подсчёт количества багов по статусу для конкретного пользователя.
     */
    @Query("SELECT b.status, COUNT(b) FROM BugReport b WHERE b.userLogin = :userLogin GROUP BY b.status")
    List<Object[]> countByStatusForUser(@Param("userLogin") String userLogin);

    /**
     * Подсчёт количества решённых (FIXED) баг-репортов.
     */
    @Query("SELECT COUNT(b) FROM BugReport b WHERE b.status = 'FIXED'")
    long countByStatusFixed();

    /**
     * Топ-10 зарегистрированных пользователей по количеству решённых багов.
     * Возвращает только пользователей с непустым логином (зарегистрированных).
     */
    @Query("SELECT b.userLogin, COUNT(b) FROM BugReport b WHERE b.status = 'FIXED' AND b.userLogin IS NOT NULL GROUP BY b.userLogin ORDER BY COUNT(b) DESC")
    List<Object[]> findTop10UsersByFixedBugs(Pageable pageable);

    /**
     * Подсчёт решённых (FIXED) баг-репортов, созданных в заданном диапазоне дат.
     * Диапазон полуоткрытый: [from, to).
     */
    @Query("SELECT COUNT(b) FROM BugReport b WHERE b.status = 'FIXED' AND b.createdAt >= :from AND b.createdAt < :to")
    long countFixedCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Топ пользователей по количеству решённых багов, созданных в заданном диапазоне дат.
     * Диапазон полуоткрытый: [from, to). Возвращает только пользователей с непустым логином.
     */
    @Query("SELECT b.userLogin, COUNT(b) FROM BugReport b WHERE b.status = 'FIXED' AND b.userLogin IS NOT NULL AND b.createdAt >= :from AND b.createdAt < :to GROUP BY b.userLogin ORDER BY COUNT(b) DESC")
    List<Object[]> findTopUsersByFixedBugsCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    /**
     * Поиск баг-репортов по логину пользователя с пагинацией.
     */
    Page<BugReport> findByUserLogin(String userLogin, Pageable pageable);

    /**
     * Поиск баг-репортов пользователя с фильтром по статусу и пагинацией.
     */
    Page<BugReport> findByUserLoginAndStatus(String userLogin, BugStatus status, Pageable pageable);

    /**
     * Подсчёт всех баг-репортов пользователя, у которых менялся статус.
     */
    @Query("SELECT COUNT(b) FROM BugReport b WHERE b.userLogin = :userLogin AND b.statusUpdatedAt IS NOT NULL")
    long countStatusUpdatesForUser(@Param("userLogin") String userLogin);

    /**
     * Подсчёт баг-репортов пользователя, статус которых меняли позже отметки `since`.
     * Вынесено в отдельный запрос, а не в условие `:since IS NULL`: Postgres не
     * может вывести тип у параметра, сравниваемого только с NULL.
     */
    @Query("SELECT COUNT(b) FROM BugReport b WHERE b.userLogin = :userLogin AND b.statusUpdatedAt > :since")
    long countStatusUpdatesForUserSince(@Param("userLogin") String userLogin, @Param("since") LocalDateTime since);

    /**
     * Самая свежая дата изменения статуса среди баг-репортов пользователя.
     * Возвращает `null`, если статус ни одного репорта ещё не меняли.
     */
    @Query("SELECT MAX(b.statusUpdatedAt) FROM BugReport b WHERE b.userLogin = :userLogin")
    LocalDateTime findLastStatusUpdatedAtForUser(@Param("userLogin") String userLogin);
}
