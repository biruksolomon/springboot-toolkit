package io.github.biruksolomon.auth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Configuration Properties for Authentication and Authorization.
 * Bind to application.yml/properties with prefix "auth".
 *
 * Example configuration:
 * <pre>
 * auth:
 *   jwt:
 *     secret: ${JWT_SECRET}
 *     accessTokenExpiry: 15m
 *     refreshTokenExpiry: 7d
 *   password:
 *     minLength: 12
 *   audit:
 *     enabled: true
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * JWT token configuration
     */
    @NestedConfigurationProperty
    private Jwt jwt = new Jwt();

    /**
     * Password policy configuration
     */
    @NestedConfigurationProperty
    private Password password = new Password();

    /**
     * Account security configuration
     */
    @NestedConfigurationProperty
    private Account account = new Account();

    /**
     * Email verification and password reset configuration
     */
    @NestedConfigurationProperty
    private Email email = new Email();

    /**
     * Multi-factor authentication (MFA/2FA) configuration
     */
    @NestedConfigurationProperty
    private Mfa mfa = new Mfa();

    /**
     * API configuration
     */
    @NestedConfigurationProperty
    private Api api = new Api();

    /**
     * Audit logging configuration
     */
    @NestedConfigurationProperty
    private Audit audit = new Audit();

    // ==================== Nested Classes ====================

    @Getter
    @Setter
    public static class Jwt {
        /**
         * JWT signing secret (must be 32+ characters)
         */
        private String secret;

        /**
         * Access token expiration duration (default: 15m)
         */
        private String accessTokenExpiry = "15m";

        /**
         * Refresh token expiration duration (default: 7d)
         */
        private String refreshTokenExpiry = "7d";

        /**
         * JWT issuer claim
         */
        private String issuer;
    }

    @Getter
    @Setter
    public static class Password {
        /**
         * Minimum password length (default: 8)
         */
        private int minLength = 8;

        /**
         * Require special characters in password (default: true)
         */
        private boolean requireSpecialChars = true;

        /**
         * Require uppercase letters in password (default: true)
         */
        private boolean requireUppercase = true;

        /**
         * Require digits in password (default: true)
         */
        private boolean requireDigit = true;

        /**
         * Number of previous passwords to prevent reuse (default: 5)
         */
        private int historyCount = 5;
    }

    @Getter
    @Setter
    public static class Account {
        /**
         * Maximum failed login attempts before account lock (default: 5)
         */
        private int maxFailedAttempts = 5;

        /**
         * Account lock duration after max failed attempts (default: 30m)
         */
        private String lockDuration = "30m";
    }

    @Getter
    @Setter
    public static class Email {
        /**
         * Require email verification on signup (default: true)
         */
        private boolean verificationRequired = true;

        /**
         * Base URL for email verification links
         */
        private String baseUrl;

        /**
         * Email verification link expiration in minutes (default: 1440 = 24h)
         */
        private int verificationExpiryMinutes = 1440;

        /**
         * Password reset link expiration in minutes (default: 60 = 1h)
         */
        private int passwordResetExpiryMinutes = 60;
    }

    @Getter
    @Setter
    public static class Mfa {
        /**
         * Issuer name for TOTP/2FA (appears in authenticator apps)
         */
        private String issuer;

        /**
         * TOTP time step in seconds (default: 30, RFC 6238)
         */
        private int timeStepSeconds = 30;

        /**
         * Number of time steps allowed for clock drift tolerance (default: 1)
         */
        private int allowedClockDriftSteps = 1;
    }

    @Getter
    @Setter
    public static class Api {
        /**
         * Base path for API endpoints (default: /api/v1)
         */
        private String basePath = "/api/v1";
    }

    /**
     * Audit logging configuration.
     * Controls audit trail creation for authentication events.
     */
    @Getter
    @Setter
    @Configuration
    @EnableConfigurationProperties(AuthProperties.class)
    public static class Audit {
        /**
         * Enable audit logging for authentication events (default: false)
         */
        private boolean enabled = false;
    }
}