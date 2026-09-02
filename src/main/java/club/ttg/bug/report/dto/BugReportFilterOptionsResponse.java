package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO со значениями для фильтров списка баг-репортов в админке.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Значения для фильтров списка баг-репортов")
public class BugReportFilterOptionsResponse {

    @Schema(description = "Логины авторов баг-репортов (без анонимов), по алфавиту")
    private List<String> userLogins;

    @Schema(description = "Логины пользователей, менявших статус баг-репортов, по алфавиту")
    private List<String> statusUpdatedByLogins;
}
