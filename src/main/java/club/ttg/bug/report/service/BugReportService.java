package club.ttg.bug.report.service;

import club.ttg.bug.report.dto.BugCountByStatusResponse;
import club.ttg.bug.report.dto.BugReportFilterOptionsResponse;
import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.dto.BugReportStatsResponse;
import club.ttg.bug.report.dto.BugReportUpdateStatusRequest;
import club.ttg.bug.report.dto.BugReportUpdateTypeRequest;
import club.ttg.bug.report.dto.MyBugReportResponse;
import club.ttg.bug.report.dto.MyBugUpdatesResponse;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.ReportType;
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
     * @param type фильтр по типу записи (может быть null — тогда все типы)
     * @param pageable параметры пагинации
     * @return страница баг-репортов
     */
    Page<BugReportResponse> getAll(BugStatus status, SourcePlatform sourcePlatform, String userLogin,
                                   String statusUpdatedBy, ReportType type, Pageable pageable);

    /**
     * Значения для фильтров списка баг-репортов: логины авторов и тех, кто менял статус.
     *
     * @param type тип записи (может быть null — тогда все типы)
     * @return списки логинов по алфавиту
     */
    BugReportFilterOptionsResponse getFilterOptions(ReportType type);

    /**
     * Обновление статуса баг-репорта.
     *
     * @param id идентификатор бага
     * @param request новый статус
     * @param updatedBy логин пользователя, изменившего статус
     * @return обновлённый баг-репорт
     */
    BugReportResponse updateStatus(UUID id, BugReportUpdateStatusRequest request, String updatedBy);

    /**
     * Смена типа записи (баг или пожелание).
     * Статус и данные о его изменении не трогаются.
     *
     * @param id идентификатор записи
     * @param request новый тип
     * @return обновлённая запись
     */
    BugReportResponse updateType(UUID id, BugReportUpdateTypeRequest request);

    StoredFile getScreenshot(UUID id);

    /**
     * Получение количества багов для пользователя с группировкой по статусу.
     *
     * @param userLogin логин пользователя
     * @param type фильтр по типу записи (может быть null — тогда все типы)
     * @return список количества багов по статусам
     */
    List<BugCountByStatusResponse> countByStatusForUser(String userLogin, ReportType type);

    /**
     * Получение баг-репортов текущего пользователя с пагинацией.
     * Ответ не содержит данных о том, кто менял статус.
     *
     * @param userLogin логин пользователя
     * @param status фильтр по статусу (может быть null)
     * @param type фильтр по типу записи (может быть null — тогда все типы)
     * @param pageable параметры пагинации
     * @return страница баг-репортов пользователя
     */
    Page<MyBugReportResponse> getByUser(String userLogin, BugStatus status, ReportType type, Pageable pageable);

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
     * Учитываются только баги, пожелания в статистику не попадают.
     *
     * @return статистика: общее количество, количество решённых, топ-10 пользователей
     */
    BugReportStatsResponse getStats();
}
