package com.alex.bank.mapper;

import com.alex.bank.dto.notification.NotificationDTO;
import com.alex.bank.dto.notification.NotificationDTOAdmin;
import com.alex.bank.dto.notification.NotificationDetailsDTO;
import com.alex.bank.models.notification.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationDTO toDTO(Notification notification);

    NotificationDTOAdmin toDTOAdmin(Notification notification);

    NotificationDetailsDTO toDetailsDTO(Notification notification);

    Notification toEntity(NotificationDTO notificationDTO);

    Notification toEntity(NotificationDTOAdmin notificationDTOAdmin);

    NotificationDTO toDTO(NotificationDetailsDTO notificationDetailsDTO);

    NotificationDTOAdmin toDTOAdmin(NotificationDetailsDTO notificationDetailsDTO);

    List<NotificationDTO> mapDetailsListToDTO(List<NotificationDetailsDTO> notificationDetailsDTOs);

    List<NotificationDTOAdmin> mapDetailsListToDTOAdmin(List<NotificationDetailsDTO> notificationDetailsDTOs);

    List<NotificationDTOAdmin> mapListToDTOAdmin(List<Notification> notifications);

    List<NotificationDetailsDTO> mapListToDetailsDTO(List<Notification> notifications);

}
