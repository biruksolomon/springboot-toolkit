package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.MfaSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfaSetupRepository extends JpaRepository<MfaSetup, Long> {
    Optional<MfaSetup> findByUserId(Long userId);
    boolean existsByUserIdAndEnabledTrue(Long userId);
}
