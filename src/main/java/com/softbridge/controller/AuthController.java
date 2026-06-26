package com.softbridge.controller;

import com.softbridge.dto.request.LoginRequest;
import com.softbridge.dto.request.RegisterRequest;
import com.softbridge.dto.response.ApiResponse;
import com.softbridge.dto.response.AuthResponse;
import com.softbridge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new client or developer account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse auth = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created successfully", auth));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", auth));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (client-side token invalidation)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT is stateless — actual invalidation is done client-side by discarding the token.
        // For server-side invalidation, a token blacklist (Redis) would be added in Phase 3.
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }
}
