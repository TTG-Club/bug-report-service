package club.ttg.bug.report.repository;

import club.ttg.bug.report.model.BugReport;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с баг-репортами.
 */
@Repository
public interface BugReportRepository extends JpaRepository<BugReport, UUID> {

    /**
     * Поиск баг-репортов по статусу.
     */
    Page<BugReport> findByStatus(BugStatus status, Pageable pageable);

    /**
     * Поиск баг-репортов по платформе-источнику.
     */
    Page<BugReport> findBySourcePlatform(SourcePlatform sourcePlatform, Pageable pageable);

    /**
     * Поиск баг-репортов по статусу и платформе.
     */
    Page<BugReport> findByStatusAndSourcePlatform(BugStatus status, SourcePlatform sourcePlatform, Pageable pageable);

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
     * Поиск баг-репортов по логину пользователя с пагинацией.
     */
    Page<BugReport> findByUserLogin(String userLogin, Pageable pageable);
}
