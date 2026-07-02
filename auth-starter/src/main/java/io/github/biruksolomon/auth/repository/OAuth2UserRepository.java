package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.OAuth2User;
import io.github.biruksolomon.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2UserRepository extends JpaRepository<OAuth2User, Long> {
    Optional<OAuth2User> findByProviderAndProviderId(String provider, String providerId);
    List<OAuth2User> findByUser(User user);
    boolean existsByProviderAndProviderId(String provider, String providerId);
}
