package io.github.biruksolomon.auth.event;

/**
 * Canonical set of auth event types fired through Spring's
 * {@link org.springframework.context.ApplicationEventPublisher}.
 * The audit-log branch subscribes to these via {@code @EventListener}.
 */
public enum AuthEventType {
    REGISTER,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGIN_LOCKED,
    LOGOUT,
    TOKEN_REFRESHED,
    PASSWORD_CHANGED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    EMAIL_VERIFICATION_SENT,
    EMAIL_VERIFIED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    MFA_ENABLED,
    MFA_DISABLED,
    MFA_VERIFIED,
    API_KEY_CREATED,
    API_KEY_REVOKED
}
