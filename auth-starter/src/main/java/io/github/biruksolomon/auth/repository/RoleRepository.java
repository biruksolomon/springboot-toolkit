package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Set<Role> findByNameIn(Set<String> names);

    /** Used by RbacService.setRolesForUser */
    List<Role> findAllByNameIn(Set<String> names);
}
