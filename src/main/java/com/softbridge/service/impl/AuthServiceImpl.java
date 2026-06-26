package com.softbridge.service.impl;

import com.softbridge.dto.request.LoginRequest;
import com.softbridge.dto.request.RegisterRequest;
import com.softbridge.dto.response.AuthResponse;
import com.softbridge.entity.User;
import com.softbridge.enums.Role;
import com.softbridge.exception.BadRequestException;
import com.softbridge.exception.ConflictException;
import com.softbridge.repository.UserRepository;
import com.softbridge.security.JwtUtil;
import com.softbridge.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Prevent duplicate emails
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }

        // Prevent client from self-registering as ADMIN
        Role role = req.getRole() == null ? Role.CLIENT : req.getRole();
        if (role == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered.");
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .company(req.getCompany())
                .phone(req.getPhone())
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("New user registered: {} ({})", user.getEmail(), user.getRole());

        String token = jwtUtil.generateToken(buildExtraClaims(user), user);
        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail().toLowerCase(), req.getPassword()));
        } catch (DisabledException e) {
            throw new BadRequestException("Account is deactivated. Contact support.");
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("User not found."));

        String token = jwtUtil.generateToken(buildExtraClaims(user), user);
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, token);
    }

    // ── helpers ──
    private Map<String, Object> buildExtraClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        return claims;
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .company(user.getCompany())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
