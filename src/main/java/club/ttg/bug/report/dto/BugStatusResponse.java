package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления статуса баг-репорта.
 */
@Data
@AllArgsConstructor
@Schema(description = "Статус баг-репорта")
public class BugStatusResponse {

    /**
     * Код статуса (enum value).
     */
    @Schema(description = "Код статуса", example = "NEW")
    private String code;

    /**
     * Русское название статуса.
     */
    @Schema(description = "Русское название статуса", example = "новый")
    private String name;
}
