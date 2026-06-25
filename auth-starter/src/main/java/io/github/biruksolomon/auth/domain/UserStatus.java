package io.github.biruksolomon.auth.domain;

/**
 * Lifecycle status of a user account.
 */
public enum UserStatus {
    /** Normal, active account. */
    ACTIVE,
    /** Temporarily locked due to too many failed login attempts. */
    LOCKED,
    /** Permanently disabled by an administrator. */
    DISABLED,
    /** Soft-deleted — excluded from all normal queries. */
    DELETED
}
