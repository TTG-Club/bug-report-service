package club.ttg.bug.report.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * Клиент для взаимодействия с внешним сервисом авторизации.
 * Отправляет JWT-токен на валидацию и получает информацию о пользователе и его ролях.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalAuthClient {

    private final WebClient webClient;

    @Value("${app.auth.url}")
    private String authServiceUrl;

    @Value("${app.auth.validate-endpoint}")
    private String validateEndpoint;

    /**
     * Валидирует JWT-токен через внешний сервис авторизации.
     *
     * @param token JWT-токен для валидации
     * @return Optional с данными пользователя, если токен валиден; пустой Optional иначе
     */
    public Optional<AuthValidationResponse> validateToken(String token) {
        try {
            AuthValidationResponse response = webClient.get()
                    .uri(authServiceUrl + validateEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(AuthValidationResponse.class)
                    .block();

            if (response != null && response.isValid()) {
                return Optional.of(response);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Ошибка при валидации токена через внешний сервис: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
