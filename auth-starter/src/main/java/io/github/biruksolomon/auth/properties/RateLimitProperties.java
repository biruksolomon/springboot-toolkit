package io.github.biruksolomon.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.rate-limit")
@Data
public class RateLimitProperties {
    private Login login = new Login();
    private PasswordReset passwordReset = new PasswordReset();
    private Api api = new Api();

    @Data
    public static class Login {
        private int attempts = 5;
        private long windowSeconds = 900; // 15 minutes
    }

    @Data
    public static class PasswordReset {
        private int attempts = 3;
        private long windowSeconds = 3600; // 1 hour
    }

    @Data
    public static class Api {
        private int limit = 1000;
        private long windowSeconds = 60; // Per minute
    }
}
