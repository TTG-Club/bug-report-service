package com.bugtracker.controller;

import com.bugtracker.dto.BugReportCreateRequest;
import com.bugtracker.dto.BugReportResponse;
import com.bugtracker.dto.BugReportUpdateStatusRequest;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.SourcePlatform;
import com.bugtracker.ratelimit.RateLimiter;
import com.bugtracker.service.BugReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST-контроллер для управления баг-репортами.
 */
@RestController
@RequestMapping("/api/v1/bugs")
@RequiredArgsConstructor
public class BugReportController {

    private final BugReportService bugReportService;
    private final RateLimiter rateLimiter;

    /**
     * Создание нового баг-репорта.
     * Принимает multipart/form-data с данными бага и скриншотом.
     *
     * @param request данные баг-репорта
     * @param screenshot скриншот (опционально)
     * @return созданный баг-репорт
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BugReportResponse> create(
            @Valid @RequestPart("request") BugReportCreateRequest request,
            @RequestPart(value = "screenshot", required = false) MultipartFile screenshot) {

        // Определяем ключ для rate limiting и тип пользователя
        boolean authenticated = request.getUserLogin() != null && !request.getUserLogin().isBlank();
        String rateLimitKey = authenticated ? request.getUserLogin() : request.getSessionId();

        if (rateLimitKey == null || rateLimitKey.isBlank()) {
            rateLimitKey = "anonymous";
        }

        rateLimiter.checkRateLimit(rateLimitKey, authenticated);

        BugReportResponse response = bugReportService.create(request, screenshot);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение баг-репорта по ID.
     *
     * @param id идентификатор бага
     * @return баг-репорт
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<BugReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bugReportService.getById(id));
    }

    /**
     * Получение списка баг-репортов с фильтрацией и пагинацией.
     *
     * @param status фильтр по статусу
     * @param sourcePlatform фильтр по платформе
     * @param pageable параметры пагинации
     * @return страница баг-репортов
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<BugReportResponse>> getAll(
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) SourcePlatform sourcePlatform,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(bugReportService.getAll(status, sourcePlatform, pageable));
    }

    /**
     * Обновление статуса баг-репорта.
     *
     * @param id идентификатор бага
     * @param request новый статус
     * @return обновлённый баг-репорт
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<BugReportResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody BugReportUpdateStatusRequest request) {
        return ResponseEntity.ok(bugReportService.updateStatus(id, request));
    }
}
