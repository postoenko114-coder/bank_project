package com.alex.bank.unit.controller;

import com.alex.bank.controllers.SupportMessageController;
import com.alex.bank.dto.support.SupportDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.supportMessage.SupportMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SupportMessageController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class SupportMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupportMessageService supportMessageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void createSupportMessage_ShouldReturnSupportDTO_WhenDataIsValid() throws Exception {
        SupportDTO fakeDTO = new SupportDTO();
        fakeDTO.setSubject("Test subject");
        fakeDTO.setMessage("Test message");
        fakeDTO.setUserEmail("user@gmail.com");

        Principal mockPrincipal = new UsernamePasswordAuthenticationToken("user@gmail.com", "password");

        when(supportMessageService.createSupportMessage(any(SupportDTO.class), any()))
                .thenReturn(fakeDTO);

        mockMvc.perform(post("/api/v1/support")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Test subject"))
                .andExpect(jsonPath("$.message").value("Test message"));

        verify(supportMessageService).createSupportMessage(any(SupportDTO.class), any());
    }

    @Test
    @WithMockUser
    void createSupportMessage_ShouldReturn400_WhenEmailMissingAndNoPrincipal() throws Exception {
        SupportDTO fakeDTO = new SupportDTO();
        fakeDTO.setSubject("Test");
        fakeDTO.setMessage("Test message");

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to input email"))
                .when(supportMessageService).createSupportMessage(any(SupportDTO.class), any());

        mockMvc.perform(post("/api/v1/support")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeDTO)))
                .andExpect(status().isBadRequest());
    }
}