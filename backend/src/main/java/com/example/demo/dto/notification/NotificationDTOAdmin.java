package com.example.demo.dto.notification;

import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTOAdmin {
    private Long id;

    private TypeNotification typeNotification;

    private String message;

    private LocalDateTime createdAt;

    private StatusNotification statusNotification;

}
