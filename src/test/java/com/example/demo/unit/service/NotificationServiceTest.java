package com.example.demo.unit.service;

import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.notification.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationServiceImpl;

    @Test
    void createNotification_ShouldReturnNotification_WhenDataIsCorrect() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationServiceImpl.createNotification(fakeUser, TypeNotification.PERSONAL, "Test message");

        assertNotNull(result);
        assertEquals(TypeNotification.PERSONAL, result.getType());
        assertEquals("Test message", result.getMessage());
        assertEquals(StatusNotification.NEW, result.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }


    @Test
    void getNotificationByIdAndMarkAsRead_ShouldReturnNotificationAndMarkAsRead_WhenNotificationExists() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setStatus(StatusNotification.SENT);
        fakeNotification.setMessage("Test");
        fakeNotification.setType(TypeNotification.PERSONAL);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        Notification result = notificationServiceImpl.getNotificationByIdAndMarkAsRead(1L);

        assertNotNull(result);
        assertEquals(StatusNotification.READ, result.getStatus());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void getNotificationByIdAndMarkAsRead_ShouldThrowNotFound_WhenNotificationDoesNotExist() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.getNotificationByIdAndMarkAsRead(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Notification not found", exception.getReason());
    }

    @Test
    void updateNotification_ShouldReturnUpdatedNotification_WhenNotificationExistsAndDataChanged() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setType(TypeNotification.PERSONAL);
        fakeNotification.setMessage("Old message");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        Notification result = notificationServiceImpl.updateNotification(1L, "DEPOSIT", "New message");

        assertNotNull(result);
        assertEquals(TypeNotification.DEPOSIT, result.getType());
        assertEquals("New message", result.getMessage());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void updateNotification_ShouldReturnSameNotification_WhenNothingChanged() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setType(TypeNotification.PERSONAL);
        fakeNotification.setMessage("Same message");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        Notification result = notificationServiceImpl.updateNotification(1L, "PERSONAL", "Same message");

        assertNotNull(result);
        assertEquals(TypeNotification.PERSONAL, result.getType());
        assertEquals("Same message", result.getMessage());
    }

    @Test
    void updateNotification_ShouldThrowNotFound_WhenNotificationDoesNotExist() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.updateNotification(1L, "PERSONAL", "msg"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Notification not found", exception.getReason());
    }

    @Test
    void getNotificationForAdmin_ShouldReturnNotificationDTOAdmin_WhenNotificationExists() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setType(TypeNotification.PERSONAL);
        fakeNotification.setStatus(StatusNotification.NEW);
        fakeNotification.setMessage("Test");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        NotificationDTOAdmin result = notificationServiceImpl.getNotificationForAdmin(1L);

        assertNotNull(result);
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void getNotificationForAdmin_ShouldThrowNotFound_WhenNotificationDoesNotExist() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.getNotificationForAdmin(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Notification not found", exception.getReason());
    }

    @Test
    void notifyPersonalMessage_ShouldCreateAndSendNotification_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(new Notification()));

        notificationServiceImpl.notifyPersonalMessage(1L, "Hello!");

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void notifyPersonalMessage_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.notifyPersonalMessage(1L, "Hello!"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void notifyAdvertisingMessage_ShouldCreateAndSendNotification_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(new Notification()));

        notificationServiceImpl.notifyAdvertisingMessage(1L, "Special offer!");

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void notifyAdvertisingMessage_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.notifyAdvertisingMessage(1L, "Promo!"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void notifyDeposit_ShouldCreateAndSendDepositNotification() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setAccountNumber("123456789/0001");
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setBalance(BigDecimal.valueOf(600));
        fakeAccount.setUser(fakeUser);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(new Notification()));

        notificationServiceImpl.notifyDeposit(fakeAccount, BigDecimal.valueOf(100));

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void notifyWithdrawal_ShouldCreateAndSendWithdrawalNotification() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setAccountNumber("123456789/0001");
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setBalance(BigDecimal.valueOf(400));
        fakeAccount.setUser(fakeUser);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(new Notification()));

        notificationServiceImpl.notifyWithdrawal(fakeAccount, BigDecimal.valueOf(100));

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Notification.class));
    }

    @Test
    void getUnreadCount_ShouldReturnCount_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(notificationRepository.countNotificationsByUserAndStatusNotification(fakeUser, StatusNotification.SENT)).thenReturn(3L);

        Long result = notificationServiceImpl.getUnreadCount(1L);

        assertEquals(3L, result);
        verify(notificationRepository, times(1)).countNotificationsByUserAndStatusNotification(fakeUser, StatusNotification.SENT);
    }

    @Test
    void getUnreadCount_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.getUnreadCount(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }


    @Test
    void getAllUserNotification_ShouldReturnNotificationList_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Notification notif1 = new Notification();
        Notification notif2 = new Notification();
        List<Notification> fakeList = List.of(notif1, notif2);

        Pageable pageable = Pageable.unpaged();

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(notificationRepository.findAllByUser(fakeUser, pageable)).thenReturn(fakeList);

        List<Notification> result = notificationServiceImpl.getAllUserNotification(1L, pageable);

        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findAllByUser(fakeUser, pageable);
    }

    @Test
    void getAllUserNotification_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.getAllUserNotification(1L, Pageable.unpaged()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void deleteNotification_ShouldCallRepositoryDelete() {
        notificationServiceImpl.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAllNotificationInUser_ShouldCallRepositoryDeleteAll_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

        notificationServiceImpl.deleteAllNotificationInUser(1L);

        verify(notificationRepository, times(1)).deleteAllByUser(fakeUser);
    }

    @Test
    void deleteAllNotificationInUser_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> notificationServiceImpl.deleteAllNotificationInUser(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

}
