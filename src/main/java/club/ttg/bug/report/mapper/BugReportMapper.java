package club.ttg.bug.report.mapper;

import club.ttg.bug.report.dto.BugReportCreateRequest;
import club.ttg.bug.report.dto.BugReportResponse;
import club.ttg.bug.report.model.BugReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Маппер для преобразования между сущностью BugReport и DTO.
 * Использует MapStruct для генерации кода маппинга.
 */
@Mapper(componentModel = "spring")
public interface BugReportMapper {

    /**
     * Преобразование сущности в DTO ответа.
     */
    @Mapping(target = "screenshotUrl", source = "screenshotPath")
    BugReportResponse toResponse(BugReport bugReport);

    /**
     * Преобразование запроса на создание в сущность.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "screenshotPath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "statusUpdatedAt", ignore = true)
    @Mapping(target = "statusComment", ignore = true)
    BugReport toEntity(BugReportCreateRequest request);
}
