package club.ttg.bug.report.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

public record S3Properties(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey
) {

    public static S3Properties from(ConfigurableEnvironment environment) {
        return new S3Properties(
                required(environment, "app.s3.endpoint", "SPRING_CLOUD_AWS_S3_ENDPOINT"),
                required(environment, "app.s3.region", "SPRING_CLOUD_AWS_S3_REGION"),
                required(environment, "app.s3.bucket", "SPRING_CLOUD_AWS_S3_BUCKET"),
                required(environment, "app.s3.access-key", "SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY"),
                required(environment, "app.s3.secret-key", "SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY")
        );
    }

    private static String required(ConfigurableEnvironment environment, String propertyName, String environmentName) {
        String value = rawProperty(environment, environmentName);
        if (!StringUtils.hasText(value)) {
            value = rawProperty(environment, propertyName);
        }

        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("S3 property '" + propertyName
                    + "' must be configured via environment variable '" + environmentName + "'.");
        }

        value = value.trim();
        if (value.startsWith("${")) {
            throw new IllegalStateException("S3 property '" + propertyName
                    + "' contains unresolved deployment template '" + value
                    + "'. Set '" + environmentName + "' to the real runtime value.");
        }

        return value;
    }

    private static String rawProperty(ConfigurableEnvironment environment, String key) {
        String systemValue = System.getenv(key);
        if (StringUtils.hasText(systemValue)) {
            return systemValue;
        }

        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            Object value = propertySource.getProperty(key);
            if (value != null) {
                return value.toString();
            }
        }

        return null;
    }
}
