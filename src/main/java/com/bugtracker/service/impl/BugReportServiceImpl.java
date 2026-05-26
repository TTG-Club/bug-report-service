package com.bugtracker.service.impl;

import com.bugtracker.dto.BugReportCreateRequest;
import com.bugtracker.dto.BugReportResponse;
import com.bugtracker.dto.BugReportUpdateStatusRequest;
import com.bugtracker.exception.BugReportNotFoundException;
import com.bugtracker.mapper.BugReportMapper;
import com.bugtracker.model.BugReport;
import com.bugtracker.model.BugStatus;
import com.bugtracker.model.SourcePlatform;
import com.bugtracker.repository.BugReportRepository;
import com.bugtracker.service.BugReportService;
import com.bugtracker.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public BugReportResponse create(BugReportCreateRequest request, MultipartFile screenshot) {
        BugReport bugReport = bugReportMapper.toEntity(request);

        if (screenshot != null && !screenshot.isEmpty()) {
            String key = fileStorageService.store(screenshot);
            String screenshotUrl = fileStorageService.getFileUrl(key);
            bugReport.setScreenshotPath(screenshotUrl);
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
}
