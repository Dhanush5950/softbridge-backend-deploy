package com.softbridge.controller;

import com.softbridge.dto.request.DecisionRequest;
import com.softbridge.dto.request.RequirementRequest;
import com.softbridge.dto.request.UpdateRequirementRequest;
import com.softbridge.dto.response.ApiResponse;
import com.softbridge.dto.response.DashboardStatsResponse;
import com.softbridge.dto.response.PageResponse;
import com.softbridge.dto.response.RequirementResponse;
import com.softbridge.entity.User;
import com.softbridge.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requirements")
@RequiredArgsConstructor
@Tag(name = "Requirements", description = "Submit and manage software requirements")
public class RequirementController {

    private final RequirementService requirementService;

    // ────────────────────────────────────────
    //  CLIENT endpoints
    // ────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Submit a new software requirement (Client only)")
    public ResponseEntity<ApiResponse<RequirementResponse>> submit(
            @Valid @RequestBody RequirementRequest request,
            @AuthenticationPrincipal User currentUser) {

        RequirementResponse response = requirementService.submit(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Requirement submitted successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get current client's own requirements")
    public ResponseEntity<ApiResponse<PageResponse<RequirementResponse>>> getMyRequirements(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<RequirementResponse> data =
                requirementService.getMyRequirements(currentUser.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Get a specific requirement belonging to the current client")
    public ResponseEntity<ApiResponse<RequirementResponse>> getMyRequirementById(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {

        RequirementResponse data =
                requirementService.getMyRequirementById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ────────────────────────────────────────
    //  ADMIN endpoints
    // ────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all requirements with optional status filter and search (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<RequirementResponse>>> getAll(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(required = false)    String status,
            @RequestParam(required = false)    String query) {

        PageResponse<RequirementResponse> data =
                requirementService.getAll(page, size, status, query);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @Operation(summary = "Get a requirement by ID")
    public ResponseEntity<ApiResponse<RequirementResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(requirementService.getById(id)));
    }

    // ────────────────────────────────────────
    //  DEVELOPER endpoints
    // ────────────────────────────────────────

    @GetMapping("/inhouse")
    @PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
    @Operation(summary = "Get requirements marked INHOUSE (Developer + Admin)")
    public ResponseEntity<ApiResponse<PageResponse<RequirementResponse>>> getInHouse(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        // Always forces status=INHOUSE — developers cannot see PENDING or
        // OUTSOURCE requirements through this endpoint, only what's been
        // approved for internal development.
        PageResponse<RequirementResponse> data =
                requirementService.getAll(page, size, "INHOUSE", null);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a requirement (Admin only)")
    public ResponseEntity<ApiResponse<RequirementResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateRequirementRequest request) {

        RequirementResponse updated = requirementService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Requirement updated", updated));
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Make In-House or Outsource decision (Admin only)")
    public ResponseEntity<ApiResponse<RequirementResponse>> makeDecision(
            @PathVariable String id,
            @Valid @RequestBody DecisionRequest request) {

        RequirementResponse result = requirementService.makeDecision(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Decision applied successfully", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a requirement (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        requirementService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Requirement deleted", null));
    }

    // ────────────────────────────────────────
    //  STATS
    // ────────────────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics (Admin only)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(requirementService.getDashboardStats()));
    }
}
