package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO со статистикой по баг-репортам.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Статистика по баг-репортам")
public class BugReportStatsResponse {

    @Schema(description = "Общее количество баг-репортов", example = "150")
    private long totalCount;

    @Schema(description = "Количество решённых баг-репортов (статус FIXED)", example = "42")
    private long fixedCount;

    @Schema(description = "Топ-10 пользователей по количеству решённых багов")
    private List<UserFixedCountResponse> topFixers;
}
