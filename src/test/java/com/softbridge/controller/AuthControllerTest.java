package com.softbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softbridge.dto.request.LoginRequest;
import com.softbridge.dto.request.RegisterRequest;
import com.softbridge.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired MockMvc    mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    @DisplayName("POST /auth/register → 201 Created")
    void register_shouldReturn201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane.doe@test.com");
        req.setPassword("Test@1234");
        req.setCompany("TestCorp");
        req.setRole(Role.CLIENT);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CLIENT"));
    }

    @Test
    @DisplayName("POST /auth/register with duplicate email → 409 Conflict")
    void register_duplicateEmail_shouldReturn409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Dup");
        req.setLastName("User");
        req.setEmail("dup@test.com");
        req.setPassword("Test@1234");
        req.setRole(Role.CLIENT);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andDo(result -> {
                    System.out.println("STATUS = " + result.getResponse().getStatus());
                    System.out.println("BODY   = " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isCreated());

        // duplicate
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/login with bad credentials → 400")
    void login_badCredentials_shouldReturn400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@test.com");
        req.setPassword("wrongpass");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/register with missing fields → 400 Validation")
    void register_missingFields_shouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();  // all blank

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").isMap());
    }
}
