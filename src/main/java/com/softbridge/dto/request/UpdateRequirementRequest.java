package com.softbridge.dto.request;

import com.softbridge.enums.Priority;
import com.softbridge.enums.RequirementStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRequirementRequest {

    @Size(max = 300)
    private String projectName;

    @Size(max = 100)
    private String projectType;

    private String description;
    private String timeline;

    private List<String> frontendStack;
    private List<String> backendStack;
    private List<String> databaseStack;
    private List<String> deploymentStack;

    private String budget;
    private String teamSize;
    private String specialFeatures;
    private Priority priority;
    private RequirementStatus status;
    private String adminNotes;
}
