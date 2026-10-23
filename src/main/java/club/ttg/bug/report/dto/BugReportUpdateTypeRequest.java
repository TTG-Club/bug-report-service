package club.ttg.bug.report.dto;

import club.ttg.bug.report.model.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для смены типа записи (баг или пожелание).
 */
@Data
@Schema(description = "Запрос на смену типа записи")
public class BugReportUpdateTypeRequest {

    /**
     * Новый тип записи.
     */
    @NotNull(message = "Тип обязателен")
    @Schema(description = "Новый тип записи", example = "WISH", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReportType type;
}
