package com.alex.bank.unit.service;

import com.alex.bank.dto.notification.NotificationDTO;
import com.alex.bank.dto.notification.NotificationDetailsDTO;
import com.alex.bank.mapper.NotificationMapper;
import com.alex.bank.mapper.NotificationMapperImpl;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.notification.Notification;
import com.alex.bank.models.notification.StatusNotification;
import com.alex.bank.models.notification.TypeNotification;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.NotificationRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.notification.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Spy
    private NotificationMapper notificationMapper = new NotificationMapperImpl();

    @Test
    void createNotification_ShouldReturnNotification_WhenDataIsCorrect() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationDetailsDTO result = notificationServiceImpl.createNotification(fakeUser, TypeNotification.PERSONAL, "Test message");

        assertNotNull(result);
        assertEquals(TypeNotification.PERSONAL, result.getTypeNotification());
        assertEquals("Test message", result.getMessage());
        assertEquals(StatusNotification.NEW, result.getStatusNotification());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }


    @Test
    void getNotificationByIdAndMarkAsRead_ShouldReturnNotificationAndMarkAsRead_WhenNotificationExists() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setStatusNotification(StatusNotification.SENT);
        fakeNotification.setMessage("Test");
        fakeNotification.setTypeNotification(TypeNotification.PERSONAL);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        NotificationDetailsDTO result = notificationServiceImpl.getNotificationByIdAndMarkAsRead(1L);

        assertNotNull(result);
        assertEquals(StatusNotification.READ, result.getStatusNotification());
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
        fakeNotification.setTypeNotification(TypeNotification.PERSONAL);
        fakeNotification.setMessage("Old message");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        NotificationDTO result = notificationServiceImpl.updateNotification(1L, "DEPOSIT", "New message");

        assertNotNull(result);
        assertEquals(TypeNotification.DEPOSIT, result.getTypeNotification());
        assertEquals("New message", result.getMessage());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void updateNotification_ShouldReturnSameNotification_WhenNothingChanged() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setTypeNotification(TypeNotification.PERSONAL);
        fakeNotification.setMessage("Same message");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        NotificationDTO result = notificationServiceImpl.updateNotification(1L, "PERSONAL", "Same message");

        assertNotNull(result);
        assertEquals(TypeNotification.PERSONAL, result.getTypeNotification());
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
    void getNotificationForAdmin_ShouldReturnNotificationDetailsDTO_WhenNotificationExists() {
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        fakeNotification.setTypeNotification(TypeNotification.PERSONAL);
        fakeNotification.setStatusNotification(StatusNotification.NEW);
        fakeNotification.setMessage("Test");

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        NotificationDetailsDTO result = notificationServiceImpl.getNotificationForAdmin(1L);

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
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(NotificationDetailsDTO.class));
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
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(NotificationDetailsDTO.class));
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
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(NotificationDetailsDTO.class));
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
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(NotificationDetailsDTO.class));
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

        List<NotificationDetailsDTO> result = notificationServiceImpl.getAllUserNotification(1L, pageable);

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
        Notification fakeNotification = new Notification();
        fakeNotification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(fakeNotification));

        notificationServiceImpl.deleteNotification(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).delete(fakeNotification);
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
