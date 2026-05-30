package com.alex.bank.dto.notification;

import com.alex.bank.models.notification.StatusNotification;
import com.alex.bank.models.notification.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTOAdmin {
    private Long id;

    private TypeNotification typeNotification;

    private String message;

    private LocalDateTime createdAt;

    private StatusNotification statusNotification;

}
