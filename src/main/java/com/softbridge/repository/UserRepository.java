package com.softbridge.repository;

import com.softbridge.entity.User;
import com.softbridge.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveByRole(@Param("role") Role role);

    
    @Query("SELECT COUNT(r) FROM Requirement r WHERE r.client.id = :userId")
    long countRequirementsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email)     LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.company)   LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> search(@Param("q") String query);
}
