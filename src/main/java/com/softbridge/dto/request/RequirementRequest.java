package com.softbridge.dto.request;

import com.softbridge.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RequirementRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 300)
    private String projectName;

    @NotBlank(message = "Project type is required")
    @Size(max = 100)
    private String projectType;

    @NotBlank(message = "Description is required")
    private String description;

    private String timeline;

    private List<String> frontendStack;
    private List<String> backendStack;
    private List<String> databaseStack;
    private List<String> deploymentStack;

    private String budget;
    private String teamSize;
    private String specialFeatures;
    private Priority priority = Priority.MEDIUM;
}
