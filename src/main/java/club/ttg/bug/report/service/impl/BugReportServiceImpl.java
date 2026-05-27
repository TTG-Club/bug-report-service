package club.ttg.bug.report.service.impl;

import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.dto.BugReportUpdateStatusRequest;
import club.ttg.bug.report.exception.BugReportNotFoundException;
import club.ttg.bug.report.mapper.BugReportMapper;
import club.ttg.bug.report.model.BugReport;
import club.ttg.bug.report.model.BugStatus;
import club.ttg.bug.report.model.SourcePlatform;
import club.ttg.bug.report.repository.BugReportRepository;
import club.ttg.bug.report.service.BugReportService;
import club.ttg.bug.report.service.FileStorageService;
import club.ttg.bug.report.service.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса управления баг-репортами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BugReportServiceImpl implements BugReportService {

    private final BugReportRepository bugReportRepository;
    private final BugReportMapper bugReportMapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public BugReportResponse create(BugReportCreateRequest request, MultipartFile screenshot, String userLogin) {
        BugReport bugReport = bugReportMapper.toEntity(request);
        bugReport.setUserLogin(userLogin);

        if (screenshot != null && !screenshot.isEmpty()) {
            String key = fileStorageService.store(screenshot);
            bugReport.setScreenshotPath(key);
        }

        BugReport saved = bugReportRepository.save(bugReport);
        log.info("Создан баг-репорт: id={}, platform={}", saved.getId(), saved.getSourcePlatform());

        return bugReportMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BugReportResponse getById(UUID id) {
        BugReport bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new BugReportNotFoundException("Баг-репорт не найден: " + id));
        return bugReportMapper.toResponse(bugReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BugReportResponse> getAll(BugStatus status, SourcePlatform sourcePlatform, Pageable pageable) {
        Page<BugReport> page;

        if (status != null && sourcePlatform != null) {
            page = bugReportRepository.findByStatusAndSourcePlatform(status, sourcePlatform, pageable);
        } else if (status != null) {
            page = bugReportRepository.findByStatus(status, pageable);
        } else if (sourcePlatform != null) {
            page = bugReportRepository.findBySourcePlatform(sourcePlatform, pageable);
        } else {
            page = bugReportRepository.findAll(pageable);
        }

        return page.map(bugReportMapper::toResponse);
    }

    @Override
    @Transactional
    public BugReportResponse updateStatus(UUID id, BugReportUpdateStatusRequest request) {
        BugReport bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new BugReportNotFoundException("Баг-репорт не найден: " + id));

        bugReport.setStatus(request.getStatus());
        bugReport.setStatusUpdatedAt(LocalDateTime.now());
        bugReport.setStatusComment(request.getComment());
        BugReport updated = bugReportRepository.save(bugReport);
        log.info("Обновлён статус баг-репорта: id={}, newStatus={}", id, request.getStatus());

        return bugReportMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StoredFile getScreenshot(UUID id) {
        BugReport bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new BugReportNotFoundException("Р‘Р°Рі-СЂРµРїРѕСЂС‚ РЅРµ РЅР°Р№РґРµРЅ: " + id));

        if (!StringUtils.hasText(bugReport.getScreenshotPath())) {
            throw new BugReportNotFoundException("РЎРєСЂРёРЅС€РѕС‚ РЅРµ РЅР°Р№РґРµРЅ: " + id);
        }

        return fileStorageService.get(bugReport.getScreenshotPath());
    }
}
