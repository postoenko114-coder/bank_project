package com.alex.bank.unit.controller.client;

import com.alex.bank.controllers.client.NotificationController;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NotificationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationMapperImpl.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private NotificationDetailsDTO buildNotificationDetailsDTO(Long id, TypeNotification type, String message) {
        NotificationDetailsDTO n = new NotificationDetailsDTO();
        n.setId(id);
        n.setTypeNotification(type);
        n.setMessage(message);
        n.setStatusNotification(StatusNotification.NEW);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }

    @Test
    @WithMockUser
    void getAllNotifications_ShouldReturnNotificationDTOList_WhenUserExists() throws Exception {
        NotificationDetailsDTO n1 = buildNotificationDetailsDTO(1L, TypeNotification.PERSONAL, "Hello");
        NotificationDetailsDTO n2 = buildNotificationDetailsDTO(2L, TypeNotification.DEPOSIT, "Deposit done");

        when(notificationService.getAllUserNotification(eq(1L), any())).thenReturn(List.of(n1, n2));

        mockMvc.perform(get("/api/v1/{userId}/notifications", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(notificationService).getAllUserNotification(eq(1L), any());
    }

    @Test
    @WithMockUser
    void getAllNotifications_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(notificationService).getAllUserNotification(eq(99L), any());

        mockMvc.perform(get("/api/v1/{userId}/notifications", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getNotification_ShouldReturnNotificationDTO_WhenNotificationExists() throws Exception {
        NotificationDetailsDTO fakeNotif = buildNotificationDetailsDTO(1L, TypeNotification.PERSONAL, "Test message");

        when(notificationService.getNotificationByIdAndMarkAsRead(1L)).thenReturn(fakeNotif);

        mockMvc.perform(get("/api/v1/{userId}/notifications/{notificationId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test message"));

        verify(notificationService).getNotificationByIdAndMarkAsRead(1L);
    }

    @Test
    @WithMockUser
    void getNotification_ShouldReturn404_WhenNotificationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).getNotificationByIdAndMarkAsRead(99L);

        mockMvc.perform(get("/api/v1/{userId}/notifications/{notificationId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUnreadCount_ShouldReturnCount_WhenUserExists() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/{userId}/notifications/unread-count", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(notificationService).getUnreadCount(1L);
    }

    @Test
    @WithMockUser
    void deleteAllNotifications_ShouldReturn204_WhenUserExists() throws Exception {
        mockMvc.perform(delete("/api/v1/{userId}/notifications", 1L))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteAllNotificationInUser(1L);
    }

    @Test
    @WithMockUser
    void deleteAllNotifications_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(notificationService).deleteAllNotificationInUser(99L);

        mockMvc.perform(delete("/api/v1/{userId}/notifications", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteNotification_ShouldReturn204_WhenNotificationExists() throws Exception {
        mockMvc.perform(delete("/api/v1/{userId}/notifications/{notificationId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(1L);
    }

    @Test
    @WithMockUser
    void deleteNotification_ShouldReturn404_WhenNotificationDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).deleteNotification(99L);

        mockMvc.perform(delete("/api/v1/{userId}/notifications/{notificationId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }
}
