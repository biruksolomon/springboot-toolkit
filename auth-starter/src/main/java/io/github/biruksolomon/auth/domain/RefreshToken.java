package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted refresh token — enables rotation and per-device revocation.
 * A new token is issued every time the refresh endpoint is called;
 * the old token is immediately invalidated (rotation).
 */
@Getter @Setter
@Entity
@Table(name = "auth_refresh_tokens",
        indexes = {
                @Index(name = "idx_art_token_hash", columnList = "token_hash"),
                @Index(name = "idx_art_user_id",    columnList = "user_id")
        })
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** SHA-256 hash of the raw token — never store the raw value. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_art_user_id"))
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Device / user-agent info for display in session management UI. */
    @Column(name = "device_info", length = 512)
    private String deviceInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); }

    protected RefreshToken() {}

    public RefreshToken(String tokenHash, User user, Instant expiresAt, String deviceInfo) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
    }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }

}
