package club.ttg.bug.report.ratelimit;

import club.ttg.bug.report.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Компонент для ограничения частоты запросов (rate limiting).
 * <p>
 * Незарегистрированные пользователи: 1 баг в минуту.
 * Зарегистрированные пользователи: 1 баг в 10 секунд.
 */
@Component
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Проверяет, может ли пользователь отправить баг-репорт.
     *
     * @param key уникальный ключ пользователя (логин или sessionId)
     * @param authenticated является ли пользователь авторизованным
     * @throws RateLimitExceededException если лимит превышен
     */
    public void checkRateLimit(String key, boolean authenticated) {
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> createBucket(authenticated));

        if (!bucket.tryConsume(1)) {
            String limit = authenticated ? "1 баг в 10 секунд" : "1 баг в минуту";
            throw new RateLimitExceededException(
                    "Превышен лимит отправки баг-репортов. Лимит: " + limit
            );
        }
    }

    private Bucket createBucket(boolean authenticated) {
        Bandwidth bandwidth;
        if (authenticated) {
            // Зарегистрированный пользователь: 1 запрос в 10 секунд
            bandwidth = Bandwidth.builder()
                    .capacity(1)
                    .refillIntervally(1, Duration.ofSeconds(10))
                    .build();
        } else {
            // Незарегистрированный пользователь: 1 запрос в минуту
            bandwidth = Bandwidth.builder()
                    .capacity(1)
                    .refillIntervally(1, Duration.ofMinutes(1))
                    .build();
        }
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
