package club.ttg.bug.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для пользователя и количества решённых багов.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Пользователь и количество решённых багов")
public class UserFixedCountResponse {

    @Schema(description = "Логин пользователя", example = "john_doe")
    private String login;

    @Schema(description = "Количество решённых багов", example = "12")
    private Long fixed;
}
