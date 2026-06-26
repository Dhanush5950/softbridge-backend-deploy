// ═══════════════════════════════════════════
//  REQUEST DTOs
// ═══════════════════════════════════════════

// ── RegisterRequest.java ──
package com.softbridge.dto.request;

import com.softbridge.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 200)
    private String company;

    @Pattern(regexp = "^\\+?[\\d\\s\\-()]{7,15}$", message = "Invalid phone number")
    private String phone;

    private Role role = Role.CLIENT;
}
