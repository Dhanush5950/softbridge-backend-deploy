package com.softbridge.entity;

import com.softbridge.enums.Priority;
import com.softbridge.enums.RequirementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a software requirement submitted by a Client.
 * Tech stacks are stored as JSON arrays in TEXT columns.
 */
@Entity
@Table(name = "requirements")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Requirement {

    @Id
    @Column(name = "id", length = 20)
    private String id;   // Format: REQ-000001

    // ── Relationships ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // ── Project Info ──
    @Column(name = "project_name", nullable = false, length = 300)
    private String projectName;

    @Column(name = "project_type", nullable = false, length = 100)
    private String projectType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "timeline", length = 50)
    private String timeline;

    // ── Tech Stacks (JSON arrays stored as TEXT) ──
    @Column(name = "frontend_stack", columnDefinition = "TEXT")
    private String frontendStack;      // e.g. ["React","Tailwind CSS"]

    @Column(name = "backend_stack", columnDefinition = "TEXT")
    private String backendStack;

    @Column(name = "database_stack", columnDefinition = "TEXT")
    private String databaseStack;

    @Column(name = "deployment_stack", columnDefinition = "TEXT")
    private String deploymentStack;

    // ── Extras ──
    @Column(name = "budget", length = 100)
    private String budget;

    @Column(name = "team_size", length = 100)
    private String teamSize;

    @Column(name = "special_features", columnDefinition = "TEXT")
    private String specialFeatures;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // ── Status ──
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private RequirementStatus status = RequirementStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // ── Timestamps ──
    @Column(name = "submitted_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;   // when admin made the decision

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt   = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
