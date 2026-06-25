package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Core user entity.  The table name uses the configurable {@code auth_} prefix;
 * the default DDL is managed by Liquibase changelog 0001.
 */
@Getter
@Setter
@Entity
@Table(name = "auth_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_auth_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_auth_users_username", columnNames = "username")
        })
public class User {

    // ── Getters / Setters ────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // ── Email verification ───────────────────────────────────────────
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // ── Account state ────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ── Soft delete ──────────────────────────────────────────────────
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ── MFA ─────────────────────────────────────────────────────────
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    // ── Audit timestamps ─────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    // ── Roles (populated in later branch) ────────────────────────────
    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "auth_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // ── Lifecycle callbacks ──────────────────────────────────────────
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── Constructors ─────────────────────────────────────────────────
    protected User() {}

    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // ── Business methods ─────────────────────────────────────────────

    public boolean isAccountNonLocked() {
        if (status == UserStatus.LOCKED) {
            if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
                // time-based auto-unlock — caller should persist this change
                this.status = UserStatus.ACTIVE;
                this.failedLoginAttempts = 0;
                this.lockedUntil = null;
                return true;
            }
            return false;
        }
        return true;
    }

    public void incrementFailedAttempts(int maxAttempts, java.time.Duration lockDuration) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.status = UserStatus.LOCKED;
            this.lockedUntil = Instant.now().plus(lockDuration);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
            this.lockedUntil = null;
        }
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.status = UserStatus.DELETED;
    }

}
