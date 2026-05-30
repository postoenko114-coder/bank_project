package com.alex.bank.unit.controller.admin;

import com.alex.bank.controllers.admin.AdminNotificationController;
import com.alex.bank.dto.notification.NotificationDetailsDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.mapper.NotificationMapperImpl;
import com.alex.bank.models.notification.StatusNotification;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminNotificationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationMapperImpl.class)
public class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserNotifications_ShouldReturnNotificationDTOAdminList_WhenUserExists() throws Exception {
        NotificationDetailsDTO n1 = buildNotificationDetailsDTO(1L, TypeNotification.PERSONAL, "Hello");
        NotificationDetailsDTO n2 = buildNotificationDetailsDTO(2L, TypeNotification.DEPOSIT, "Deposit done");

        when(notificationService.getAllUserNotification(eq(1L), any())).thenReturn(List.of(n1, n2));

        mockMvc.perform(get("/api/v1/admin/{userId}/notifications", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService).getAllUserNotification(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserNotifications_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(notificationService).getAllUserNotification(eq(99L), any());

        mockMvc.perform(get("/api/v1/admin/{userId}/notifications", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserNotification_ShouldReturnNotificationDTOAdmin_WhenNotificationExists() throws Exception {
        NotificationDetailsDTO fakeDTO = new NotificationDetailsDTO();
        fakeDTO.setMessage("Test");

        when(notificationService.getNotificationForAdmin(1L)).thenReturn(fakeDTO);

        mockMvc.perform(get("/api/v1/admin/{userId}/notifications/{notificationId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test"));

        verify(notificationService).getNotificationForAdmin(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserNotification_ShouldReturn404_WhenNotificationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).getNotificationForAdmin(99L);

        mockMvc.perform(get("/api/v1/admin/{userId}/notifications/{notificationId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendNotification_ShouldReturnNotificationDTOAdmin_WhenUserExists() throws Exception {
        NotificationDetailsDTO fakeNotification = buildNotificationDetailsDTO(1L, TypeNotification.PERSONAL, "Admin message");

        when(notificationService.createAndSendNotification(eq(1L), eq(TypeNotification.PERSONAL), eq("Admin message")))
                .thenReturn(fakeNotification);

        mockMvc.perform(post("/api/v1/admin/{userId}/notifications/sendNotification", 1L)
                        .param("message", "Admin message")
                        .param("typeNotification", "PERSONAL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Admin message"));

        verify(notificationService).createAndSendNotification(eq(1L), eq(TypeNotification.PERSONAL), eq("Admin message"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendNotification_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(notificationService).createAndSendNotification(eq(99L), eq(TypeNotification.PERSONAL), eq("Hello"));

        mockMvc.perform(post("/api/v1/admin/{userId}/notifications/sendNotification", 99L)
                        .param("message", "Hello")
                        .param("typeNotification", "PERSONAL"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendNotification_ShouldReturn400_WhenParamsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/admin/{userId}/notifications/sendNotification", 1L))
                .andExpect(status().isBadRequest());
    }

    private NotificationDetailsDTO buildNotificationDetailsDTO(Long id, TypeNotification type, String message) {
        NotificationDetailsDTO n = new NotificationDetailsDTO();
        n.setId(id);
        n.setTypeNotification(type);
        n.setMessage(message);
        n.setStatusNotification(StatusNotification.NEW);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }
}
