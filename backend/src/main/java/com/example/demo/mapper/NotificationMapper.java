package com.example.demo.mapper;

import com.example.demo.dto.notification.NotificationDTO;
import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.notification.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationDTO toDTO(Notification notification);

    NotificationDTOAdmin toDTOAdmin(Notification notification);

    Notification toEntity(NotificationDTO notificationDTO);

    Notification toEntity(NotificationDTOAdmin notificationDTOAdmin);

}
