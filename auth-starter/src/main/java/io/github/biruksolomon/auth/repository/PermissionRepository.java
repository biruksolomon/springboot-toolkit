package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findAllByNameIn(Set<String> names);

    @Query("""
    SELECT DISTINCT p
    FROM User u
    JOIN u.roles r
    JOIN r.permissions p
    WHERE u.id = :userId
    """)
    List<Permission> findAllByUserId(@Param("userId") UUID userId);
}
