package com.medisphere.auth.repository;

import com.medisphere.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.msUserId = :msUserId")
    void deleteByMsUserId(@Param("msUserId") String msUserId);

    long countByRole(String role);

    @Query("SELECT MAX(CAST(SUBSTRING(u.msUserId, 3) AS long)) FROM User u WHERE u.role = :role")
    Optional<Long> findMaxIdByRole(@Param("role") String role);

    Optional<User> findByMsUserId(String msUserId);
}
