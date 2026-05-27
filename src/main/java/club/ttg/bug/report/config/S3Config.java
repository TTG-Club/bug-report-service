package club.ttg.bug.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Конфигурация клиента AWS S3 для работы с S3-совместимым хранилищем (Beget Cloud).
 */
@Configuration
public class S3Config {

    @Bean
    public S3Properties s3Properties(ConfigurableEnvironment environment) {
        return S3Properties.from(environment);
    }

    /**
     * Создает клиент S3 с настройками для S3-совместимого хранилища.
     *
     * @return настроенный экземпляр S3Client
     */
    @Bean
    public S3Client s3Client(S3Properties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
                ))
                .forcePathStyle(true)
                .build();
    }
}
