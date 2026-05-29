package club.ttg.bug.report.service;

import club.ttg.bug.report.dto.BugCountByStatusResponse;
import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.dto.BugReportStatsResponse;
import club.ttg.bug.report.dto.BugReportUpdateStatusRequest;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления баг-репортами.
 */
public interface BugReportService {

    /**
     * Создание нового баг-репорта.
     *
     * @param request данные баг-репорта
     * @param screenshot скриншот (может быть null)
     * @return созданный баг-репорт
     */
    BugReportResponse create(BugReportCreateRequest request, MultipartFile screenshot, String userLogin);

    /**
     * Получение баг-репорта по ID.
     *
     * @param id идентификатор бага
     * @return баг-репорт
     */
    BugReportResponse getById(UUID id);

    /**
     * Получение списка баг-репортов с фильтрацией.
     *
     * @param status фильтр по статусу (может быть null)
     * @param sourcePlatform фильтр по платформе (может быть null)
     * @param pageable параметры пагинации
     * @return страница баг-репортов
     */
    Page<BugReportResponse> getAll(BugStatus status, SourcePlatform sourcePlatform, Pageable pageable);

    /**
     * Обновление статуса баг-репорта.
     *
     * @param id идентификатор бага
     * @param request новый статус
     * @return обновлённый баг-репорт
     */
    BugReportResponse updateStatus(UUID id, BugReportUpdateStatusRequest request);

    StoredFile getScreenshot(UUID id);

    /**
     * Получение количества багов для пользователя с группировкой по статусу.
     *
     * @param userLogin логин пользователя
     * @return список количества багов по статусам
     */
    List<BugCountByStatusResponse> countByStatusForUser(String userLogin);

    /**
     * Получение всех баг-репортов текущего пользователя с пагинацией.
     *
     * @param userLogin логин пользователя
     * @param pageable параметры пагинации
     * @return страница баг-репортов пользователя
     */
    Page<BugReportResponse> getByUser(String userLogin, Pageable pageable);

    /**
     * Получение общей статистики по баг-репортам.
     *
     * @return статистика: общее количество, количество решённых, топ-10 пользователей
     */
    BugReportStatsResponse getStats();
}
