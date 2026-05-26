package com.bugtracker.service;

import com.bugtracker.dto.BugReportCreateRequest;
import com.bugtracker.dto.BugReportResponse;
import com.bugtracker.dto.BugReportUpdateStatusRequest;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.SourcePlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
    BugReportResponse create(BugReportCreateRequest request, MultipartFile screenshot);

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
}
