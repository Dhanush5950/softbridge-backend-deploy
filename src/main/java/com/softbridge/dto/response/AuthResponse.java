package com.softbridge.dto.response;

import com.softbridge.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Returned on successful login or register.
 */
@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long   expiresIn;       // ms

    private Long   userId;
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private Role   role;

    private LocalDateTime createdAt;
}
