package club.ttg.bug.report.service;

import club.ttg.bug.report.dto.BugCountByStatusResponse;
import club.ttg.bug.report.dto.BugReportFilterOptionsResponse;
import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.dto.BugReportStatsResponse;
import club.ttg.bug.report.dto.BugReportUpdateStatusRequest;
import club.ttg.bug.report.dto.MyBugReportResponse;
import club.ttg.bug.report.dto.MyBugUpdatesResponse;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
     * @param userLogin фильтр по логину автора (может быть null)
     * @param statusUpdatedBy фильтр по логину пользователя, последним менявшего статус (может быть null)
     * @param pageable параметры пагинации
     * @return страница баг-репортов
     */
    Page<BugReportResponse> getAll(BugStatus status, SourcePlatform sourcePlatform, String userLogin,
                                   String statusUpdatedBy, Pageable pageable);

    /**
     * Значения для фильтров списка баг-репортов: логины авторов и тех, кто менял статус.
     *
     * @return списки логинов по алфавиту
     */
    BugReportFilterOptionsResponse getFilterOptions();

    /**
     * Обновление статуса баг-репорта.
     *
     * @param id идентификатор бага
     * @param request новый статус
     * @param updatedBy логин пользователя, изменившего статус
     * @return обновлённый баг-репорт
     */
    BugReportResponse updateStatus(UUID id, BugReportUpdateStatusRequest request, String updatedBy);

    StoredFile getScreenshot(UUID id);

    /**
     * Получение количества багов для пользователя с группировкой по статусу.
     *
     * @param userLogin логин пользователя
     * @return список количества багов по статусам
     */
    List<BugCountByStatusResponse> countByStatusForUser(String userLogin);

    /**
     * Получение баг-репортов текущего пользователя с пагинацией.
     * Ответ не содержит данных о том, кто менял статус.
     *
     * @param userLogin логин пользователя
     * @param status фильтр по статусу (может быть null)
     * @param pageable параметры пагинации
     * @return страница баг-репортов пользователя
     */
    Page<MyBugReportResponse> getByUser(String userLogin, BugStatus status, Pageable pageable);

    /**
     * Сводка изменений по баг-репортам пользователя для индикатора «есть новости».
     *
     * @param userLogin логин пользователя
     * @param since отметка последнего просмотра (может быть null — тогда считаются все изменения)
     * @return количество непросмотренных изменений и самая свежая дата изменения
     */
    MyBugUpdatesResponse getMyUpdates(String userLogin, LocalDateTime since);

    /**
     * Получение общей статистики по баг-репортам.
     *
     * @return статистика: общее количество, количество решённых, топ-10 пользователей
     */
    BugReportStatsResponse getStats();
}
