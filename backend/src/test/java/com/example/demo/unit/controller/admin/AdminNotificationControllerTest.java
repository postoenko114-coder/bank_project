package com.example.demo.unit.controller.admin;

import com.example.demo.controllers.admin.AdminNotificationController;
import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.security.JwtService;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminNotificationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserNotifications_ShouldReturnNotificationDTOAdminList_WhenUserExists() throws Exception {
        Notification n1 = buildNotification(1L, TypeNotification.PERSONAL, "Hello");
        Notification n2 = buildNotification(2L, TypeNotification.DEPOSIT, "Deposit done");

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
        NotificationDTOAdmin fakeDTO = new NotificationDTOAdmin();
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
        User fakeUser = new User();
        fakeUser.setId(1L);

        Notification fakeNotification = buildNotification(1L, TypeNotification.PERSONAL, "Admin message");

        when(userService.getUserById(1L)).thenReturn(fakeUser);
        when(notificationService.createNotification(eq(fakeUser), eq(TypeNotification.PERSONAL), eq("Admin message")))
                .thenReturn(fakeNotification);

        mockMvc.perform(post("/api/v1/admin/{userId}/notifications/sendNotification", 1L)
                        .param("message", "Admin message")
                        .param("typeNotification", "PERSONAL"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Admin message"));

        verify(notificationService).createNotification(eq(fakeUser), eq(TypeNotification.PERSONAL), eq("Admin message"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendNotification_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).getUserById(99L);

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

    private Notification buildNotification(Long id, TypeNotification type, String message) {
        Notification n = new Notification();
        n.setId(id);
        n.setType(type);
        n.setMessage(message);
        n.setStatus(StatusNotification.NEW);
        n.setCreatedAt(LocalDateTime.now());
        return n;
    }
}
