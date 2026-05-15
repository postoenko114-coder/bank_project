package com.example.demo.integration.controller;


import com.example.demo.dto.user.UserDTO;
import com.example.demo.dto.user.UserLogin;
import com.example.demo.integration.config.IntegrationTestBase;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldSaveUserToDatabase_AndReturnToken() throws Exception {
        UserDTO request = new UserDTO();
        request.setUsername("integrationUser");
        request.setEmail("newuser@gmail.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        boolean userExists = userRepository.findByEmail("newuser@gmail.com").isPresent();
        assertTrue(userExists, "User must exists");
    }

    @Test
    void authenticate_ShouldReturnTokenAndDashboardUrl_WhenCredentialsAreValid() throws Exception {
        UserDTO setupUser = new UserDTO();
        setupUser.setUsername("loginUser");
        setupUser.setEmail("login@gmail.com");
        setupUser.setPassword("SecretPass123!");
        authService.register(setupUser);

        UserLogin loginRequest = new UserLogin();
        loginRequest.setUsername("login@gmail.com");
        loginRequest.setPassword("SecretPass123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUrl").value("/dashboard.html"))
                .andExpect(jsonPath("$.token").exists());
    }

}

