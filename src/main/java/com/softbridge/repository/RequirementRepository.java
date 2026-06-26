package com.softbridge.repository;

import com.softbridge.entity.Requirement;
import com.softbridge.enums.Priority;
import com.softbridge.enums.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, String> {

    // ── Client queries ──
    List<Requirement> findByClientIdOrderBySubmittedAtDesc(Long clientId);

    Page<Requirement> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    // ── Admin queries ──
    Page<Requirement> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    Page<Requirement> findByStatusOrderBySubmittedAtDesc(RequirementStatus status, Pageable pageable);

    Page<Requirement> findByPriorityOrderBySubmittedAtDesc(Priority priority, Pageable pageable);

    // ── Count by status ──
    long countByStatus(RequirementStatus status);

    // ── Search ──
    @Query("SELECT r FROM Requirement r WHERE " +
           "LOWER(r.projectName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(r.projectType)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(r.description)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(r.id)           LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(r.client.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Requirement> search(@Param("q") String query, Pageable pageable);

    // ── Filtered search (status + keyword) ──
    @Query("SELECT r FROM Requirement r WHERE " +
           "r.status = :status AND (" +
           "LOWER(r.projectName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(r.id)          LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Requirement> searchByStatus(@Param("status") RequirementStatus status,
                                     @Param("q") String query,
                                     Pageable pageable);

    // ── Latest ID for sequence generation ──
    @Query("SELECT r.id FROM Requirement r ORDER BY r.submittedAt DESC")
    List<String> findAllIds();

    // ── Stats ──
    @Query("SELECT r.status, COUNT(r) FROM Requirement r GROUP BY r.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT r.projectType, COUNT(r) FROM Requirement r GROUP BY r.projectType ORDER BY COUNT(r) DESC")
    List<Object[]> countGroupByProjectType();

    @Query("SELECT r.priority, COUNT(r) FROM Requirement r GROUP BY r.priority")
    List<Object[]> countGroupByPriority();
}
