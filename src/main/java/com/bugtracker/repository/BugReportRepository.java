package com.bugtracker.repository;

import com.bugtracker.model.BugReport;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.SourcePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
