package com.softbridge.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softbridge.dto.request.DecisionRequest;
import com.softbridge.dto.request.RequirementRequest;
import com.softbridge.dto.request.UpdateRequirementRequest;
import com.softbridge.dto.response.DashboardStatsResponse;
import com.softbridge.dto.response.PageResponse;
import com.softbridge.dto.response.RequirementResponse;
import com.softbridge.entity.Requirement;
import com.softbridge.entity.User;
import com.softbridge.enums.RequirementStatus;
import com.softbridge.enums.Role;
import com.softbridge.exception.BadRequestException;
import com.softbridge.exception.ResourceNotFoundException;
import com.softbridge.repository.RequirementRepository;
import com.softbridge.repository.UserRepository;
import com.softbridge.service.EmailService;
import com.softbridge.service.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementServiceImpl implements RequirementService {

    private final RequirementRepository reqRepo;
    private final UserRepository        userRepo;
    private final EmailService          emailService;
    private final ObjectMapper          objectMapper;

    // ────────────────────────────────────────
    //  CLIENT OPERATIONS
    // ────────────────────────────────────────

    @Override
    @Transactional
    public RequirementResponse submit(RequirementRequest request, Long clientId) {
        User client = userRepo.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        Requirement req = Requirement.builder()
                .id(generateReqId())
                .client(client)
                .projectName(request.getProjectName())
                .projectType(request.getProjectType())
                .description(request.getDescription())
                .timeline(request.getTimeline())
                .frontendStack(toJson(request.getFrontendStack()))
                .backendStack(toJson(request.getBackendStack()))
                .databaseStack(toJson(request.getDatabaseStack()))
                .deploymentStack(toJson(request.getDeploymentStack()))
                .budget(request.getBudget())
                .teamSize(request.getTeamSize())
                .specialFeatures(request.getSpecialFeatures())
                .priority(request.getPriority())
                .status(RequirementStatus.PENDING)
                .build();

        reqRepo.save(req);
        log.info("Requirement submitted: {} by client {}", req.getId(), clientId);

        // Async email confirmation to client
        emailService.sendSubmissionConfirmation(client.getEmail(), client.getFirstName(),
                req.getId(), req.getProjectName());

        return toResponse(req);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RequirementResponse> getMyRequirements(Long clientId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<RequirementResponse> result = reqRepo
                .findByClientId(clientId, pageable)
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public RequirementResponse getMyRequirementById(String id, Long clientId) {
        Requirement req = findById(id);
        if (!req.getClient().getId().equals(clientId)) {
            throw new BadRequestException("Access denied to requirement: " + id);
        }
        return toResponse(req);
    }

    // ────────────────────────────────────────
    //  ADMIN OPERATIONS
    // ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RequirementResponse> getAll(int page, int size, String status, String query) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());

        Page<Requirement> result;

        if (query != null && !query.isBlank() && status != null && !status.isBlank()) {
            RequirementStatus s = parseStatus(status);
            result = reqRepo.searchByStatus(s, query.trim(), pageable);
        } else if (query != null && !query.isBlank()) {
            result = reqRepo.search(query.trim(), pageable);
        } else if (status != null && !status.isBlank()) {
            result = reqRepo.findByStatusOrderBySubmittedAtDesc(parseStatus(status), pageable);
        } else {
            result = reqRepo.findAllByOrderBySubmittedAtDesc(pageable);
        }

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public RequirementResponse getById(String id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional
    public RequirementResponse update(String id, UpdateRequirementRequest request) {
        Requirement req = findById(id);

        if (request.getProjectName()  != null) req.setProjectName(request.getProjectName());
        if (request.getProjectType()  != null) req.setProjectType(request.getProjectType());
        if (request.getDescription()  != null) req.setDescription(request.getDescription());
        if (request.getTimeline()     != null) req.setTimeline(request.getTimeline());
        if (request.getFrontendStack() != null) req.setFrontendStack(toJson(request.getFrontendStack()));
        if (request.getBackendStack()  != null) req.setBackendStack(toJson(request.getBackendStack()));
        if (request.getDatabaseStack() != null) req.setDatabaseStack(toJson(request.getDatabaseStack()));
        if (request.getDeploymentStack()!= null) req.setDeploymentStack(toJson(request.getDeploymentStack()));
        if (request.getBudget()       != null) req.setBudget(request.getBudget());
        if (request.getTeamSize()     != null) req.setTeamSize(request.getTeamSize());
        if (request.getSpecialFeatures()!= null) req.setSpecialFeatures(request.getSpecialFeatures());
        if (request.getPriority()     != null) req.setPriority(request.getPriority());
        if (request.getStatus()       != null) req.setStatus(request.getStatus());
        if (request.getAdminNotes()   != null) req.setAdminNotes(request.getAdminNotes());

        reqRepo.save(req);
        log.info("Requirement updated: {}", id);
        return toResponse(req);
    }

    @Override
    @Transactional
    public RequirementResponse makeDecision(String id, DecisionRequest request) {
        RequirementStatus decision = request.getStatus();
        if (decision == RequirementStatus.PENDING) {
            throw new BadRequestException("Decision must be INHOUSE or OUTSOURCE, not PENDING.");
        }

        Requirement req = findById(id);
        req.setStatus(decision);
        req.setAdminNotes(request.getAdminNotes());
        req.setDecidedAt(LocalDateTime.now());
        reqRepo.save(req);

        log.info("Decision made on {}: {}", id, decision);

        // Async email notification to client
        emailService.sendDecisionNotification(
                req.getClient().getEmail(),
                req.getClient().getFirstName(),
                req.getId(),
                req.getProjectName(),
                decision.name(),
                request.getAdminNotes()
        );

        return toResponse(req);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Requirement req = findById(id);
        reqRepo.delete(req);
        log.info("Requirement deleted: {}", id);
    }

    // ────────────────────────────────────────
    //  STATS
    // ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long total     = reqRepo.count();
        long pending   = reqRepo.countByStatus(RequirementStatus.PENDING);
        long inhouse   = reqRepo.countByStatus(RequirementStatus.INHOUSE);
        long outsource = reqRepo.countByStatus(RequirementStatus.OUTSOURCE);
        long users     = userRepo.count();
        long clients   = userRepo.countByRole(Role.CLIENT);
        long devs      = userRepo.countByRole(Role.DEVELOPER);

        Map<String, Long> byType = reqRepo.countGroupByProjectType().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long)   row[1],
                        (a, b) -> a,
                        LinkedHashMap::new));


        Map<String, Long> byPriority = reqRepo.countGroupByPriority().stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).longValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("PENDING",   pending);
        byStatus.put("INHOUSE",   inhouse);
        byStatus.put("OUTSOURCE", outsource);

        return DashboardStatsResponse.builder()
                .totalRequirements(total)
                .pendingCount(pending)
                .inhouseCount(inhouse)
                .outsourceCount(outsource)
                .totalUsers(users)
                .clientCount(clients)
                .developerCount(devs)
                .byProjectType(byType)
                .byStatus(byStatus)
                .byPriority(byPriority)
                .build();
    }

    // ────────────────────────────────────────
    //  PRIVATE HELPERS
    // ────────────────────────────────────────

    private Requirement findById(String id) {
        return reqRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + id));
    }

    private RequirementStatus parseStatus(String s) {
        try {
            return RequirementStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + s);
        }
    }

    /** Generates the next sequential REQ-XXXXXX ID (thread-safe via transaction). */
    @Transactional
    private String generateReqId() {
    List<String> ids = reqRepo.findAllIds();
    if (ids.isEmpty()) return "REQ-000001";
    String last = ids.get(0); // already ordered by submittedAt DESC
    int num = Integer.parseInt(last.replace("REQ-", "")) + 1;
    return String.format("REQ-%06d", num);
}

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private RequirementResponse toResponse(Requirement r) {
        return RequirementResponse.from(
                r,
                fromJson(r.getFrontendStack()),
                fromJson(r.getBackendStack()),
                fromJson(r.getDatabaseStack()),
                fromJson(r.getDeploymentStack())
        );
    }
}
