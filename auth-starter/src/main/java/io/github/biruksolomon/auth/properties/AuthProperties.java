package io.github.biruksolomon.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Password password = new Password();
    private Account account = new Account();
    private Email email = new Email();
    private Mfa mfa = new Mfa();
    private OAuth2 oauth2 = new OAuth2();
    private Api api = new Api();
    private RateLimit rateLimit = new RateLimit();
    private String tablePrefix = "auth_";

    @Data
    public static class Jwt {
        private String secret;
        private Duration accessTokenExpiry = Duration.ofMinutes(15);
        private Duration refreshTokenExpiry = Duration.ofDays(7);
        private String issuer = "spring-auth-core";
    }

    @Data
    public static class Password {
        private int minLength = 8;
        private boolean requireSpecialChars = true;
        private int historyCount = 5;
    }

    @Data
    public static class Account {
        private int maxFailedAttempts = 5;
        private Duration lockDuration = Duration.ofMinutes(30);
    }

    @Data
    public static class Email {
        private boolean verificationRequired = true;
        private String verificationTokenExpiry = "24h";
        private String passwordResetTokenExpiry = "1h";
        private String smtpHost = "localhost";
        private int smtpPort = 587;
        private String fromAddress = "noreply@example.com";
    }

    @Data
    public static class Mfa {
        private boolean enabled = false;
    }

    @Data
    public static class OAuth2 {
        private boolean enabled = false;
        private List<String> providers = new ArrayList<>();
    }

    @Data
    public static class Api {
        private String basePath = "/";
    }

    @Data
    public static class RateLimit {
        private LoginRateLimit login = new LoginRateLimit();

        @Data
        public static class LoginRateLimit {
            private int requests = 10;
            private Duration per = Duration.ofMinutes(1);
        }
    }
}
