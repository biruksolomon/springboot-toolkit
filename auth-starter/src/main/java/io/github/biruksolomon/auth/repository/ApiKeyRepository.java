package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.ApiKey;
import io.github.biruksolomon.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByUser(User user);
    List<ApiKey> findByUserAndActive(User user, boolean active);
    boolean existsByKeyHashAndActive(String keyHash, boolean active);
}
