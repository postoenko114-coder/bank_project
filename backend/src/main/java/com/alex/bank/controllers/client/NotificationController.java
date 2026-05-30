package com.alex.bank.controllers.client;

import com.alex.bank.dto.notification.NotificationDTO;
import com.alex.bank.mapper.NotificationMapper;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/notifications")
@IsOwner
@RequiredArgsConstructor
@Tag(name = "Client Notifications", description = "Client notification inbox endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    private final NotificationMapper notificationMapper;

    @Operation(summary = "Get all notifications of the authenticated user")
    @GetMapping
    public List<NotificationDTO> getAllNotifications(@PathVariable Long userId, Pageable pageable) {
        return notificationMapper.mapDetailsListToDTO(notificationService.getAllUserNotification(userId, pageable));
    }

    @Operation(summary = "Get a notification and mark it as read")
    @GetMapping("/{notificationId}")
    @PreAuthorize("@userSecurity.isNotificationOwner(#userId, #notificationId) or hasRole('ADMIN')")
    public NotificationDTO getNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        return notificationMapper.toDTO(notificationService.getNotificationByIdAndMarkAsRead(notificationId));
    }

    @Operation(summary = "Get unread notification count for the authenticated user")
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "Delete all notifications of the authenticated user")
    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllNotifications(@PathVariable Long userId) {
        notificationService.deleteAllNotificationInUser(userId);
    }

    @Operation(summary = "Delete a notification of the authenticated user")
    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userSecurity.isNotificationOwner(#userId, #notificationId) or hasRole('ADMIN')")
    public void deleteNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
    }
}
