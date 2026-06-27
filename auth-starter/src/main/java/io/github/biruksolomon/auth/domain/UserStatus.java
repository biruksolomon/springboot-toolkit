package io.github.biruksolomon.auth.domain;

/**
 * Represents the status of a user account.
 *
 * - ACTIVE: User can log in and use the system
 * - LOCKED: User account is temporarily locked (failed login attempts, admin action, etc.)
 * - DISABLED: User account is disabled by admin, user cannot log in
 * - DELETED: User account is soft-deleted, not shown in listings
 */
public enum UserStatus {
    ACTIVE("active", "User account is active and can login"),
    LOCKED("locked", "User account is temporarily locked"),
    DISABLED("disabled", "User account is disabled by administrator"),
    DELETED("deleted", "User account is soft-deleted");

    private final String value;
    private final String description;

    UserStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isLocked() {
        return this == LOCKED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public static UserStatus fromValue(String value) {
        for (UserStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status: " + value);
    }
}
