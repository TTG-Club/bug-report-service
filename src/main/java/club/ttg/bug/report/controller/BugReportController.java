package club.ttg.bug.report.controller;

import club.ttg.bug.report.dto.BugCountByStatusResponse;
import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.dto.BugReportUpdateStatusRequest;
import club.ttg.bug.report.dto.BugStatusResponse;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import club.ttg.bug.report.ratelimit.RateLimiter;
import club.ttg.bug.report.service.BugReportService;
import club.ttg.bug.report.service.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления баг-репортами.
 */
@RestController
@RequestMapping("/api/v1/bugs")
@RequiredArgsConstructor
@Tag(name = "Баг-репорты", description = "Управление баг-репортами")
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
    @Operation(summary = "Создание баг-репорта", description = "Создаёт новый баг-репорт с возможностью прикрепления скриншота. Доступен без авторизации.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Баг-репорт успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "429", description = "Превышен лимит запросов")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BugReportResponse> create(
            @Valid @RequestPart("request") BugReportCreateRequest request,
            @Parameter(description = "Скриншот бага (до 10MB)")
            @RequestPart(value = "screenshot", required = false) MultipartFile screenshot,
            Authentication authentication) {

        // Определяем ключ для rate limiting и тип пользователя
        String userLogin = resolveUserLogin(authentication);
        boolean authenticated = userLogin != null;
        String rateLimitKey = authenticated ? userLogin : request.getSessionId();

        if (rateLimitKey == null || rateLimitKey.isBlank()) {
            rateLimitKey = "anonymous";
        }

        rateLimiter.checkRateLimit(rateLimitKey, authenticated);

        BugReportResponse response = bugReportService.create(request, screenshot, userLogin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String resolveUserLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String name = authentication.getName();
        return name == null || name.isBlank() ? null : name;
    }

    /**
     * Получение баг-репорта по ID.
     *
     * @param id идентификатор бага
     * @return баг-репорт
     */
    @Operation(summary = "Получение баг-репорта по ID", description = "Возвращает баг-репорт по его идентификатору. Требуется роль ADMIN или MODERATOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баг-репорт найден"),
            @ApiResponse(responseCode = "404", description = "Баг-репорт не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<BugReportResponse> getById(
            @Parameter(description = "UUID баг-репорта") @PathVariable UUID id) {
        return ResponseEntity.ok(bugReportService.getById(id));
    }

    @Operation(summary = "Получение скриншота для бага", description = "Возвращает скриншот")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Screenshot found"),
            @ApiResponse(responseCode = "404", description = "Bug report or screenshot not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}/screenshot")
    public ResponseEntity<byte[]> getScreenshot(
            @Parameter(description = "Bug report UUID") @PathVariable UUID id) {
        StoredFile screenshot = bugReportService.getScreenshot(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(screenshot.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(screenshot.content());
    }

    /**
     * Получение списка баг-репортов с фильтрацией и пагинацией.
     *
     * @param status фильтр по статусу
     * @param sourcePlatform фильтр по платформе
     * @param pageable параметры пагинации
     * @return страница баг-репортов
     */
    @Operation(summary = "Список баг-репортов", description = "Возвращает список баг-репортов с фильтрацией по статусу и платформе. Поддерживает пагинацию. Требуется роль ADMIN или MODERATOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список баг-репортов"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<BugReportResponse>> getAll(
            @Parameter(description = "Фильтр по статусу") @RequestParam(required = false) BugStatus status,
            @Parameter(description = "Фильтр по платформе-источнику") @RequestParam(required = false) SourcePlatform sourcePlatform,
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
    @Operation(summary = "Обновление статуса", description = "Обновляет статус баг-репорта с возможностью добавления комментария. Требуется роль ADMIN или MODERATOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус обновлён"),
            @ApiResponse(responseCode = "404", description = "Баг-репорт не найден"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<BugReportResponse> updateStatus(
            @Parameter(description = "UUID баг-репорта") @PathVariable UUID id,
            @Valid @RequestBody BugReportUpdateStatusRequest request) {
        return ResponseEntity.ok(bugReportService.updateStatus(id, request));
    }

    /**
     * Получение списка доступных статусов баг-репорта.
     *
     * @return список статусов с русскими названиями
     */
    @Operation(summary = "Список статусов", description = "Возвращает все доступные статусы баг-репортов с русскими названиями. Доступен без авторизации.")
    @ApiResponse(responseCode = "200", description = "Список статусов")
    @GetMapping("/statuses")
    public ResponseEntity<List<BugStatusResponse>> getStatuses() {
        List<BugStatusResponse> statuses = Arrays.stream(BugStatus.values())
                .map(s -> new BugStatusResponse(s.name(), s.getName()))
                .toList();
        return ResponseEntity.ok(statuses);
    }

    /**
     * Получение количества багов для текущего пользователя с группировкой по статусу.
     *
     * @param authentication данные аутентификации
     * @return список количества багов по статусам
     */
    @Operation(summary = "Количество багов по статусу", description = "Возвращает количество багов текущего пользователя с группировкой по статусу. Требуется авторизация.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Количество багов по статусам"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/my/count-by-status")
    public ResponseEntity<List<BugCountByStatusResponse>> countByStatusForCurrentUser(Authentication authentication) {
        String userLogin = resolveUserLogin(authentication);
        if (userLogin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bugReportService.countByStatusForUser(userLogin));
    }
}
