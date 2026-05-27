package club.ttg.bug.report.dto;

import club.ttg.bug.report.model.BugStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для количества багов по статусу.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Количество багов по статусу")
public class BugCountByStatusResponse {

    @Schema(description = "Статус бага", example = "NEW")
    private BugStatus status;

    @Schema(description = "Количество багов с данным статусом", example = "5")
    private Long count;
}
