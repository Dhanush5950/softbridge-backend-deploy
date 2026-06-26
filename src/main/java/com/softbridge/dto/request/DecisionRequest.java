package com.softbridge.dto.request;

import com.softbridge.enums.RequirementStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecisionRequest {

    @NotNull(message = "Status decision is required")
    private RequirementStatus status;   // INHOUSE or OUTSOURCE

    private String adminNotes;          // optional note from admin
}
