package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления типа записи (баг или пожелание).
 */
@Data
@AllArgsConstructor
@Schema(description = "Тип записи")
public class ReportTypeResponse {

    /**
     * Код типа (enum value).
     */
    @Schema(description = "Код типа", example = "BUG")
    private String code;

    /**
     * Русское название типа.
     */
    @Schema(description = "Русское название типа", example = "баг")
    private String name;
}
