package com.alex.bank.integration.controller;


import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.dto.user.UserLogin;
import com.alex.bank.integration.config.IntegrationTestBase;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.security.AuthenticationService;
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
        String request = """
                {
                  "username": "integrationUser",
                  "email": "newuser@gmail.com",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))

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

