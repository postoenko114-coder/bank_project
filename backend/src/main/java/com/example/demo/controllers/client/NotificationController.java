package com.example.demo.controllers.client;

import com.example.demo.dto.notification.NotificationDTO;
import com.example.demo.models.notification.Notification;
import com.example.demo.security.IsOwner;
import com.example.demo.services.notification.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/notifications")
@IsOwner
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDTO> getAllNotifications(@PathVariable Long userId, @PageableDefault(size = 10) Pageable pageable) {
        return getNotificationDTOs(notificationService.getAllUserNotification(userId, pageable));
    }

    @GetMapping("/{notificationId}")
    public NotificationDTO getNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        return notificationService.getNotificationByIdAndMarkAsRead(notificationId).toDTO();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllNotifications(@PathVariable Long userId) {
        notificationService.deleteAllNotificationInUser(userId);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(@PathVariable Long userId, @PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
    }

    private List<NotificationDTO> getNotificationDTOs(List<Notification> notifications) {
        List<NotificationDTO> list = new ArrayList<>();
        for (Notification notification : notifications) {
            list.add(notification.toDTO());
        }
        return list;
    }
}
