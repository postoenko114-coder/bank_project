package com.alex.bank.repositories;

import com.alex.bank.models.notification.Notification;
import com.alex.bank.models.notification.StatusNotification;
import com.alex.bank.models.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser(User user, Pageable pageable);

    Long countNotificationsByUserAndStatusNotification(User user, StatusNotification statusNotification);

    void deleteAllByUser(User user);

    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.id = :notificationId AND n.user.id = :userId")
    boolean existsByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}
