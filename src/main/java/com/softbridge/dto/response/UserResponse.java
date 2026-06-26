package com.softbridge.dto.response;

import com.softbridge.entity.User;
import com.softbridge.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long          id;
    private String        firstName;
    private String        lastName;
    private String        fullName;
    private String        email;
    private String        company;
    private String        phone;
    private Role          role;
    private Boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long          requirementCount;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .company(u.getCompany())
                .phone(u.getPhone())
                .role(u.getRole())
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .requirementCount(u.getRequirements() == null ? 0 : u.getRequirements().size())
                .build();
    }
}
