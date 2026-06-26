package com.softbridge.service;

import com.softbridge.dto.request.DecisionRequest;
import com.softbridge.dto.request.RequirementRequest;
import com.softbridge.dto.response.RequirementResponse;
import com.softbridge.entity.Requirement;
import com.softbridge.entity.User;
import com.softbridge.enums.Priority;
import com.softbridge.enums.RequirementStatus;
import com.softbridge.enums.Role;
import com.softbridge.exception.BadRequestException;
import com.softbridge.exception.ResourceNotFoundException;
import com.softbridge.repository.RequirementRepository;
import com.softbridge.repository.UserRepository;
import com.softbridge.service.impl.RequirementServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequirementService Unit Tests")
class RequirementServiceTest {

    @Mock RequirementRepository reqRepo;
    @Mock UserRepository        userRepo;
    @Mock EmailService          emailService;

    @InjectMocks
    RequirementServiceImpl requirementService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User   testClient;
    private Requirement testReq;

    @BeforeEach
    void setUp() {
        // inject objectMapper manually (not managed by Spring here)
        try {
            var field = RequirementServiceImpl.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(requirementService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testClient = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@test.com")
                .company("TestCorp")
                .role(Role.CLIENT)
                .active(true)
                .build();

        testReq = Requirement.builder()
                .id("REQ-000001")
                .client(testClient)
                .projectName("Test Project")
                .projectType("Web Application")
                .description("A test project description")
                .priority(Priority.MEDIUM)
                .status(RequirementStatus.PENDING)
                .frontendStack("[]")
                .backendStack("[]")
                .databaseStack("[]")
                .deploymentStack("[]")
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("submit() → saves requirement and returns response")
    void submit_shouldSaveAndReturnResponse() {
        RequirementRequest request = new RequirementRequest();
        request.setProjectName("Test Project");
        request.setProjectType("Web Application");
        request.setDescription("A test project description");
        request.setPriority(Priority.MEDIUM);
        request.setFrontendStack(List.of("React"));
        request.setBackendStack(List.of("Spring Boot"));
        request.setDatabaseStack(List.of("MySQL"));
        request.setDeploymentStack(List.of("AWS"));

        when(userRepo.findById(1L)).thenReturn(Optional.of(testClient));
        when(reqRepo.count()).thenReturn(0L);
        when(reqRepo.save(any())).thenReturn(testReq);
        doNothing().when(emailService)
                .sendSubmissionConfirmation(anyString(), anyString(), anyString(), anyString());

        RequirementResponse response = requirementService.submit(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getProjectName()).isEqualTo("Test Project");
        assertThat(response.getStatus()).isEqualTo(RequirementStatus.PENDING);
        verify(reqRepo, times(1)).save(any(Requirement.class));
    }

    @Test
    @DisplayName("submit() with invalid clientId → throws ResourceNotFoundException")
    void submit_invalidClient_throwsException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        RequirementRequest request = new RequirementRequest();
        request.setProjectName("Test");
        request.setProjectType("Web Application");
        request.setDescription("desc");

        assertThatThrownBy(() -> requirementService.submit(request, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");
    }

    @Test
    @DisplayName("getById() → returns correct requirement")
    void getById_shouldReturnRequirement() {
        when(reqRepo.findById("REQ-000001")).thenReturn(Optional.of(testReq));

        RequirementResponse response = requirementService.getById("REQ-000001");

        assertThat(response.getId()).isEqualTo("REQ-000001");
        assertThat(response.getClientName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("getById() with invalid ID → throws ResourceNotFoundException")
    void getById_invalidId_throwsException() {
        when(reqRepo.findById("REQ-999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requirementService.getById("REQ-999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("makeDecision(INHOUSE) → updates status and sends email")
    void makeDecision_inhouse_updatesStatus() {
        when(reqRepo.findById("REQ-000001")).thenReturn(Optional.of(testReq));
        when(reqRepo.save(any())).thenReturn(testReq);
        doNothing().when(emailService)
                .sendDecisionNotification(anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString());

        DecisionRequest decision = new DecisionRequest();
        decision.setStatus(RequirementStatus.INHOUSE);
        decision.setAdminNotes("We have the capacity.");

        RequirementResponse response = requirementService.makeDecision("REQ-000001", decision);

        assertThat(response).isNotNull();
        verify(reqRepo).save(argThat(r -> r.getStatus() == RequirementStatus.INHOUSE));
        verify(emailService).sendDecisionNotification(
                eq("jane@test.com"), eq("Jane"), eq("REQ-000001"),
                anyString(), eq("INHOUSE"), anyString());
    }

    @Test
    @DisplayName("makeDecision(PENDING) → throws BadRequestException")
    void makeDecision_pending_throwsException() {

        // No repository mock needed because validation fails
        // before reqRepo.findById() is called.

        DecisionRequest decision = new DecisionRequest();
        decision.setStatus(RequirementStatus.PENDING);

        assertThatThrownBy(() ->
                requirementService.makeDecision("REQ-000001", decision))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("delete() → removes requirement from repository")
    void delete_shouldRemoveRequirement() {
        when(reqRepo.findById("REQ-000001")).thenReturn(Optional.of(testReq));
        doNothing().when(reqRepo).delete(any());

        requirementService.delete("REQ-000001");

        verify(reqRepo).delete(testReq);
    }

    @Test
    @DisplayName("getMyRequirementById() with wrong client → throws BadRequestException")
    void getMyRequirementById_wrongClient_throwsException() {
        when(reqRepo.findById("REQ-000001")).thenReturn(Optional.of(testReq));

        // client ID 99 doesn't own REQ-000001 (owned by client ID 1)
        assertThatThrownBy(() -> requirementService.getMyRequirementById("REQ-000001", 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Access denied");
    }
}
