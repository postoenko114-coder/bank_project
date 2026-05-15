package com.example.demo.unit.controller.admin;

import com.example.demo.controllers.admin.AdminSupportMessageController;
import com.example.demo.dto.support.SupportDTO;
import com.example.demo.dto.support.SupportReplyDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.security.JwtService;
import com.example.demo.services.EmailService;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.supportMessage.SupportMessageService;
import com.example.demo.services.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminSupportMessageController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminSupportMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupportMessageService supportMessageService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private SupportDTO buildSupportDTO(Long id, String email, String subject) {
        SupportDTO dto = new SupportDTO();
        dto.setId(id);
        dto.setUserEmail(email);
        dto.setSubject(subject);
        dto.setMessage("Test message");
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSupportMessages_ShouldReturnSupportDTOList() throws Exception {
        when(supportMessageService.getAllSupportMessages())
                .thenReturn(List.of(buildSupportDTO(1L, "user1@gmail.com", "Issue"),
                        buildSupportDTO(2L, "user2@gmail.com", "Question")));

        mockMvc.perform(get("/api/v1/admin/supportMessages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(supportMessageService).getAllSupportMessages();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSupportMessage_ShouldReturnSupportDTO_WhenMessageExists() throws Exception {
        when(supportMessageService.getSupportMessageById(1L))
                .thenReturn(buildSupportDTO(1L, "user@gmail.com", "Help"));

        mockMvc.perform(get("/api/v1/admin/supportMessages/{supportMessageId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Help"));

        verify(supportMessageService).getSupportMessageById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSupportMessage_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"))
                .when(supportMessageService).getSupportMessageById(99L);

        mockMvc.perform(get("/api/v1/admin/supportMessages/{supportMessageId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_ShouldReturnFilteredMessages_ByEmail() throws Exception {
        when(supportMessageService.search(eq("user@gmail.com"), any()))
                .thenReturn(List.of(buildSupportDTO(1L, "user@gmail.com", "Issue")));

        mockMvc.perform(get("/api/v1/admin/supportMessages/search")
                        .param("email", "user@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(supportMessageService).search(eq("user@gmail.com"), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void search_ShouldReturnFilteredMessages_ByDate() throws Exception {
        when(supportMessageService.search(isNull(), any(LocalDate.class)))
                .thenReturn(List.of(buildSupportDTO(1L, "user@gmail.com", "Issue")));

        mockMvc.perform(get("/api/v1/admin/supportMessages/search")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void replyToMessage_ShouldSendNotification_WhenUserIsRegistered() throws Exception {
        SupportDTO fakeMessage = buildSupportDTO(1L, "registered@gmail.com", "Help");
        SupportReplyDTO replyDTO = new SupportReplyDTO();
        replyDTO.setReplyText("We are glad to help!");

        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setEmail("registered@gmail.com");

        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setTypeNotification(TypeNotification.SUPPORT);
        fakeNotification.setStatusNotification(StatusNotification.NEW);
        fakeNotification.setMessage("RE: Support Request");

        when(supportMessageService.getSupportMessageById(1L)).thenReturn(fakeMessage);
        when(userService.checkEmail("registered@gmail.com")).thenReturn(true);
        when(userService.findUserByEmail("registered@gmail.com")).thenReturn(fakeUser);
        when(notificationService.createNotification(eq(fakeUser), eq(TypeNotification.SUPPORT), anyString()))
                .thenReturn(fakeNotification);

        mockMvc.perform(post("/api/v1/admin/supportMessages/{supportMessageId}/reply", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isCreated());

        verify(notificationService).sendNotificationToClient(eq(fakeUser), any(Notification.class));
        verify(emailService, never()).sendSimpleEmail(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void replyToMessage_ShouldSendEmail_WhenUserIsNotRegistered() throws Exception {
        SupportDTO fakeMessage = buildSupportDTO(1L, "external@gmail.com", "Help");
        SupportReplyDTO replyDTO = new SupportReplyDTO();
        replyDTO.setReplyText("Thank you for reaching out!");

        when(supportMessageService.getSupportMessageById(1L)).thenReturn(fakeMessage);
        when(userService.checkEmail("external@gmail.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/supportMessages/{supportMessageId}/reply", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isCreated());

        verify(emailService).sendSimpleEmail(eq("external@gmail.com"), anyString(), anyString());
        verify(notificationService, never()).sendNotificationToClient(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void replyToMessage_ShouldReturn404_WhenEmailIsEmpty() throws Exception {
        SupportDTO fakeMessage = buildSupportDTO(1L, "", "Help");
        SupportReplyDTO replyDTO = new SupportReplyDTO();
        replyDTO.setReplyText("Reply");

        when(supportMessageService.getSupportMessageById(1L)).thenReturn(fakeMessage);

        mockMvc.perform(post("/api/v1/admin/supportMessages/{supportMessageId}/reply", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void replyToMessage_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {
        SupportReplyDTO replyDTO = new SupportReplyDTO();
        replyDTO.setReplyText("Reply");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"))
                .when(supportMessageService).getSupportMessageById(99L);

        mockMvc.perform(post("/api/v1/admin/supportMessages/{supportMessageId}/reply", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isNotFound());
    }
}
