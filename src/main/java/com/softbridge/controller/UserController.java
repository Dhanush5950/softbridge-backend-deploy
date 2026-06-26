package com.softbridge.controller;

import com.softbridge.dto.request.ChangePasswordRequest;
import com.softbridge.dto.request.UpdateUserRequest;
import com.softbridge.dto.response.ApiResponse;
import com.softbridge.dto.response.UserResponse;
import com.softbridge.entity.User;
import com.softbridge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and admin user management")
public class UserController {

    private final UserService userService;

    // ────────────────────────────────────────
    //  PROFILE  (any authenticated user)
    // ────────────────────────────────────────

    @GetMapping("/users/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(currentUser.getEmail())));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.update(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", updated));
    }

    @PatchMapping("/users/me/password")
    @Operation(summary = "Change current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    // ────────────────────────────────────────
    //  ADMIN — User Management
    // ────────────────────────────────────────

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users with optional search (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAll(query)));
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id)));
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("User updated", updated));
    }

    @PatchMapping("/admin/users/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user active/inactive status (Admin only)")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        userService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.ok("User status toggled", null));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}
