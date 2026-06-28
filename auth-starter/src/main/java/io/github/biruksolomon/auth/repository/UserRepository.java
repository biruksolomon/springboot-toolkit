package io.github.biruksolomon.auth.repository;

import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.email = :email")
    Optional<User> findActiveUserByEmail(@Param("email") String email, @Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE u.status IN ('ACTIVE', 'LOCKED')")
    List<User> findAllNonDeletedUsers();
}
