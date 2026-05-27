package club.ttg.bug.report.mapper;

import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.model.BugReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

/**
 * Маппер для преобразования между сущностью BugReport и DTO.
 * Использует MapStruct для генерации кода маппинга.
 */
@Mapper(componentModel = "spring")
public interface BugReportMapper {

    String PUBLIC_API_URL = "https://bug-report.api.ttg.club";

    /**
     * Преобразование сущности в DTO ответа.
     */
    @Mapping(target = "screenshotUrl", expression = "java(toScreenshotUrl(bugReport))")
    BugReportResponse toResponse(BugReport bugReport);

    default String toScreenshotUrl(BugReport bugReport) {
        if (bugReport == null || !StringUtils.hasText(bugReport.getScreenshotPath())) {
            return null;
        }

        return PUBLIC_API_URL + "/api/v1/bugs/" + bugReport.getId() + "/screenshot";
    }

    /**
     * Преобразование запроса на создание в сущность.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "userLogin", ignore = true)
    @Mapping(target = "screenshotPath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "statusUpdatedAt", ignore = true)
    @Mapping(target = "statusComment", ignore = true)
    BugReport toEntity(BugReportCreateRequest request);
}
