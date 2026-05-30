package com.alex.bank.controllers.admin;

import com.alex.bank.dto.notification.NotificationDTOAdmin;
import com.alex.bank.mapper.NotificationMapper;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.services.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/notifications")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Notifications", description = "Administrative notification management endpoints")
public class AdminNotificationController {


    private final NotificationService notificationService;

    private final NotificationMapper notificationMapper;

    @Operation(summary = "Get all notifications of a user as admin")
    @GetMapping
    public List<NotificationDTOAdmin> getUserNotifications(@PathVariable Long userId, Pageable pageable) {
        return notificationMapper.mapDetailsListToDTOAdmin(notificationService.getAllUserNotification(userId, pageable));
    }

    @Operation(summary = "Get a notification by id as admin")
    @GetMapping("/{notificationId}")
    public NotificationDTOAdmin getUserNotification(@PathVariable Long notificationId) {
        return notificationMapper.toDTOAdmin(notificationService.getNotificationForAdmin(notificationId));
    }

    @Operation(summary = "Create and send a notification to a user as admin")
    @PostMapping("/sendNotification")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDTOAdmin sendNotification(@PathVariable Long userId, @RequestParam String message, @RequestParam String typeNotification) {
        return notificationMapper.toDTOAdmin(notificationService.createAndSendNotification(userId, parseNotificationType(typeNotification), message));
    }

    private TypeNotification parseNotificationType(String typeNotification) {
        try {
            return TypeNotification.valueOf(typeNotification.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification type");
        }
    }

}
