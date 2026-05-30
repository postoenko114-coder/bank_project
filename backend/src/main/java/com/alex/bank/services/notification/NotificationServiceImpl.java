package com.alex.bank.services.notification;

import com.alex.bank.dto.notification.NotificationDTO;
import com.alex.bank.dto.notification.NotificationDetailsDTO;
import com.alex.bank.mapper.NotificationMapper;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.notification.Notification;
import com.alex.bank.models.notification.StatusNotification;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.NotificationRepository;
import com.alex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final NotificationMapper notificationMapper;

    @Transactional
    @Override
    public NotificationDetailsDTO createNotification(User user, TypeNotification typeNotification, String message){
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTypeNotification(typeNotification);
        notification.setStatusNotification(StatusNotification.NEW);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created notificationId={} userId={} type={}", saved.getId(), user.getId(), saved.getTypeNotification());
        return notificationMapper.toDetailsDTO(saved);
    }

    @Transactional
    @Override
    public NotificationDetailsDTO createAndSendNotification(Long userId, TypeNotification typeNotification, String message) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        NotificationDetailsDTO notification = createNotification(user, typeNotification, message);
        sendNotificationToClient(user, notification);
        log.info("Notification created and sent notificationId={} userId={} type={}", notification.getId(), userId, typeNotification);
        return notification;
    }

    @Transactional
    @Override
    public NotificationDetailsDTO getNotificationByIdAndMarkAsRead(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatusNotification(StatusNotification.READ);
        log.info("Notification marked as read notificationId={}", notification.getId());
        return notificationMapper.toDetailsDTO(notification);
    }

    @Transactional
    @Override
    public NotificationDTO updateNotification(Long notification_id, String typeNotification, String message){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        TypeNotification parsedType = parseNotificationType(typeNotification);
        if(!parsedType.equals(notification.getTypeNotification())){
            notification.setTypeNotification(parsedType);
        }
        if(!message.equals(notification.getMessage())){
            notification.setMessage(message);
        }
        return notificationMapper.toDTO(notification);
    }

    @Transactional
    @Override
    public void notifyDeposit(Account account, BigDecimal amount){
        String message = "Your account: " + account.getAccountNumber() + " has been deposited by " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + String.format("%.2f", account.getBalance());
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.DEPOSIT, message));
    }

    @Transactional
    @Override
    public void notifyWithdrawal(Account account, BigDecimal amount){
        String message = "From your account: " + account.getAccountNumber() + " has been withdrawn by " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + String.format("%.2f", account.getBalance());
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.WITHDRAWAL, message));
    }

    @Transactional
    @Override
    public void notifyTransfer(Account accountFrom, Account accountTo, BigDecimal amount){
        String messageTo = "To your account " + accountTo.getAccountNumber() + " was sent transfer from account " + accountFrom.getAccountNumber() + " for the amount " + amount + " "
                + accountTo.getCurrencyAccount() + System.lineSeparator() + "\nNew balance: " + String.format("%.2f", accountTo.getBalance());
        String messageFrom = "From your account " + accountFrom.getAccountNumber() + " was sent transfer to account " + accountTo.getAccountNumber() + " for the amount " + amount + " "
                + accountFrom.getCurrencyAccount() + System.lineSeparator() + "\nNew balance: " + String.format("%.2f", accountFrom.getBalance());
        sendNotificationToClient(accountTo.getUser(), createNotification(accountTo.getUser(), TypeNotification.TRANSFER, messageTo));
        sendNotificationToClient(accountFrom.getUser(), createNotification(accountFrom.getUser(), TypeNotification.TRANSFER, messageFrom));
    }

    @Transactional
    @Override
    public void notifyPaymentByCard(Account account, BigDecimal amount){
        String message = "Payment by card for the amount " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + account.getBalance() ;
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.CARD, message));
    }

    @Transactional
    @Override
    public void notifyPersonalMessage(Long user_id, String message){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        sendNotificationToClient(user, createNotification(user, TypeNotification.PERSONAL, message));
    }

    @Transactional
    @Override
    public void notifyAdvertisingMessage(Long user_id, String message){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        sendNotificationToClient(user, createNotification(user, TypeNotification.ADVERTISING, message));
    }

    @Transactional
    @Override
    public void sendNotificationToClient(User user, NotificationDetailsDTO notificationDetailsDTO) {
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationDetailsDTO);
        markAsSent(notificationDetailsDTO.getId());
        log.info("Notification sent notificationId={} userId={} type={}", notificationDetailsDTO.getId(), user.getId(), notificationDetailsDTO.getTypeNotification());
    }

    @Transactional
    @Override
    public NotificationDetailsDTO getNotificationForAdmin(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        return notificationMapper.toDetailsDTO(notification);
    }

    @Transactional
    @Override
    public List<NotificationDetailsDTO> getAllUserNotification(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return notificationMapper.mapListToDetailsDTO(notificationRepository.findAllByUser(user, pageable));
    }

    @Transactional
    @Override
    public Long getUnreadCount(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return notificationRepository.countNotificationsByUserAndStatusNotification(user, StatusNotification.SENT);
    }

    @Transactional
    @Override
    public void deleteNotification(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notificationRepository.delete(notification);
        log.info("Notification deleted notificationId={}", notification_id);
    }

    @Transactional
    @Override
    public void deleteAllNotificationInUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        notificationRepository.deleteAllByUser(user);
        log.info("All user notifications deleted userId={}", user_id);
    }

    public void markAsSent(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatusNotification(StatusNotification.SENT);
    }

    public void markAsRead(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatusNotification(StatusNotification.READ);
    }

    private TypeNotification parseNotificationType(String typeNotification) {
        try {
            return TypeNotification.valueOf(typeNotification.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification type");
        }
    }

}
