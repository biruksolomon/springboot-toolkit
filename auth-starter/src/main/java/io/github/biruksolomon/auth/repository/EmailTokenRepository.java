package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByToken(String token);

    List<EmailToken> findByUserId(Long userId);

    List<EmailToken> findByUserIdAndType(Long userId, EmailToken.EmailTokenType type);

    @Query("SELECT e FROM EmailToken e WHERE e.expiresAt < :now AND e.usedAt IS NULL")
    List<EmailToken> findExpiredUnusedTokens(LocalDateTime now);

    @Query("SELECT COUNT(e) FROM EmailToken e WHERE e.user.id = :userId AND e.type = :type AND e.usedAt IS NULL AND e.expiresAt > :now")
    long countValidTokens(Long userId, EmailToken.EmailTokenType type, LocalDateTime now);
}
