package com.softbridge.dto.response;

import com.softbridge.entity.Requirement;
import com.softbridge.enums.Priority;
import com.softbridge.enums.RequirementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequirementResponse {

    private String              id;
    private Long                clientId;
    private String              clientName;
    private String              clientEmail;
    private String              company;

    private String              projectName;
    private String              projectType;
    private String              description;
    private String              timeline;

    private List<String>        frontendStack;
    private List<String>        backendStack;
    private List<String>        databaseStack;
    private List<String>        deploymentStack;

    private String              budget;
    private String              teamSize;
    private String              specialFeatures;
    private Priority            priority;

    private RequirementStatus   status;
    private String              adminNotes;

    private LocalDateTime       submittedAt;
    private LocalDateTime       updatedAt;
    private LocalDateTime       decidedAt;

    public static RequirementResponse from(Requirement r,
                                           List<String> fe,
                                           List<String> be,
                                           List<String> db,
                                           List<String> dep) {
        return RequirementResponse.builder()
                .id(r.getId())
                .clientId(r.getClient().getId())
                .clientName(r.getClient().getFullName())
                .clientEmail(r.getClient().getEmail())
                .company(r.getClient().getCompany())
                .projectName(r.getProjectName())
                .projectType(r.getProjectType())
                .description(r.getDescription())
                .timeline(r.getTimeline())
                .frontendStack(fe)
                .backendStack(be)
                .databaseStack(db)
                .deploymentStack(dep)
                .budget(r.getBudget())
                .teamSize(r.getTeamSize())
                .specialFeatures(r.getSpecialFeatures())
                .priority(r.getPriority())
                .status(r.getStatus())
                .adminNotes(r.getAdminNotes())
                .submittedAt(r.getSubmittedAt())
                .updatedAt(r.getUpdatedAt())
                .decidedAt(r.getDecidedAt())
                .build();
    }
}
