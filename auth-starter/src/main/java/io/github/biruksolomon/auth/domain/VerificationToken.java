package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Single-use token persisted in the database for email verification
 * and password-reset flows.
 *
 * The raw token is never stored; only its SHA-256 digest ({@code tokenHash})
 * is written, so a DB breach cannot be used to trigger a reset.
 */
@Entity
@Table(name = "auth_verification_tokens",
        indexes = {
                @Index(name = "idx_vt_token_hash", columnList = "token_hash"),
                @Index(name = "idx_vt_user_type",  columnList = "user_id, token_type")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hex digest of the raw token sent to the user. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    public void markUsed() {
        this.usedAt = Instant.now();
    }
}
