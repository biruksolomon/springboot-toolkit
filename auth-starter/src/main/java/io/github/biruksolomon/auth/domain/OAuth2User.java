package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_oauth2_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String provider; // "GOOGLE", "GITHUB"

    @Column(nullable = false)
    private String providerId; // ID from provider

    private String email; // Email from provider (may differ from User.email)
    private String displayName;
    private String profilePictureUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime connectedAt = LocalDateTime.now();

    private LocalDateTime lastUsedAt;
}
