package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mfa_setups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaSetup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "backup_codes")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> backupCodes;

    @Column(name = "backup_codes_used")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> usedBackupCodes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime enabledAt;
}