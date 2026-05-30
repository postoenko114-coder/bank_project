package com.alex.bank.services.notification;

import com.alex.bank.dto.notification.NotificationDTO;
import com.alex.bank.dto.notification.NotificationDetailsDTO;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.models.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    @Transactional
    NotificationDetailsDTO createNotification(User user, TypeNotification typeNotification, String message);

    @Transactional
    NotificationDetailsDTO createAndSendNotification(Long userId, TypeNotification typeNotification, String message);

    @Transactional
    NotificationDetailsDTO getNotificationByIdAndMarkAsRead(Long notification_id);

    @Transactional
    NotificationDTO updateNotification(Long notification_id, String typeNotification, String message);

    @Transactional
    void notifyDeposit(Account account, BigDecimal amount);

    @Transactional
    void notifyWithdrawal(Account account, BigDecimal amount);

    @Transactional
    void notifyTransfer(Account accountTo, Account accountFrom, BigDecimal amount);

    @Transactional
    void notifyPaymentByCard(Account account, BigDecimal amount);

    @Transactional
    void notifyPersonalMessage(Long user_id, String message);

    @Transactional
    void notifyAdvertisingMessage(Long user_id, String message);

    @Transactional
    void sendNotificationToClient(User user, NotificationDetailsDTO notificationDetailsDTO);

    @Transactional
    NotificationDetailsDTO getNotificationForAdmin(Long notification_id);

    @Transactional
    List<NotificationDetailsDTO> getAllUserNotification(Long user_id, Pageable pageable);

    @Transactional
    Long getUnreadCount(Long userId);

    @Transactional
    void deleteNotification(Long notification_id);

    @Transactional
    void deleteAllNotificationInUser(Long user_id);
}
