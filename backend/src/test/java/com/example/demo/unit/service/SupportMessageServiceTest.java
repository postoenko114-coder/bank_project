package com.example.demo.unit.service;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.models.supportMessage.StatusSupportMessage;
import com.example.demo.models.supportMessage.SupportMessage;
import com.example.demo.models.user.User;
import com.example.demo.repositories.SupportMessageRepository;
import com.example.demo.services.supportMessage.SupportMessageServiceImpl;
import com.example.demo.services.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SupportMessageServiceTest {

    @Mock
    private SupportMessageRepository  supportMessageRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SupportMessageServiceImpl supportMessageServiceImpl;

    @Test
    void createSupportMessage_ShouldReturnSupportDTO_WhenPrincipalIsNotNull() {
        Principal principal = mock(Principal.class);
        String userEmail = "test@gmail.com";

        User fakeUser = new User();
        fakeUser.setUsername("alex");
        fakeUser.setEmail("test@gmail.com");

        SupportDTO dto = new SupportDTO();
        dto.setMessage("Test");
        dto.setSubject("Test");

        when(principal.getName()).thenReturn("test@gmail.com");
        when(userService.findUserByEmail(userEmail)).thenReturn(fakeUser);

        SupportDTO result = supportMessageServiceImpl.createSupportMessage(dto, principal);

        assertNotNull(result);
        assertEquals(dto.getMessage(), result.getMessage());
        assertEquals(dto.getSubject(), result.getSubject());
        assertEquals(fakeUser.getEmail(), result.getUserEmail());

        verify(userService, times(1)).findUserByEmail(userEmail);
        verify(principal, times(1)).getName();
    }

    @Test
    void createSupportMessage_ShouldThrowsException_WhenPrincipalIsNullAndEmailIsEmpty() {
        SupportDTO dto = new SupportDTO();
        java.security.Principal principal = null;
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> supportMessageServiceImpl.createSupportMessage(dto, principal) );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("You need to input email", exception.getReason());
    }

    @Test
    void createSupportMessage_ShouldReturnSupportDTO_WhenPrincipalIsNullAndEmailIsNotEmpty() {
        SupportDTO dto = new SupportDTO();
        dto.setMessage("Test");
        dto.setSubject("Test");
        dto.setUserEmail("test@gmail.com");

        java.security.Principal principal = null;

        SupportDTO result = supportMessageServiceImpl.createSupportMessage(dto, principal);

        assertNotNull(result);
        assertEquals(dto.getMessage(), result.getMessage());
        assertEquals(dto.getSubject(), result.getSubject());
        assertEquals(dto.getUserEmail(), result.getUserEmail());
    }

    @Test
    void getSupportById_ShouldReturnSupportDTO_WhenSupportIsPresent() {
        SupportMessage fakeSupportMessage = new SupportMessage();
        fakeSupportMessage.setId(1L);
        fakeSupportMessage.setMessage("Test");

        when(supportMessageRepository.findSupportMessageById(1L)).thenReturn(Optional.of(fakeSupportMessage));

        SupportDTO result = supportMessageServiceImpl.getSupportMessageById(1L);

        assertNotNull(result);
        assertEquals(fakeSupportMessage.getMessage(), result.getMessage());
        verify(supportMessageRepository, times(1)).findSupportMessageById(1L);
    }

    @Test
    void getSupportById_ShouldThrowsException_WhenSupportIsNotPresent() {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> supportMessageServiceImpl.getSupportMessageById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Support Message not found", exception.getReason());
    }

    @Test
    void getAllSupportMessages_ShouldReturnSupportDTO_WhenSupportIsPresent() {
        SupportMessage fakeSupportMessage1 = new SupportMessage();
        fakeSupportMessage1.setId(1L);

        SupportMessage fakeSupportMessage2 = new SupportMessage();
        fakeSupportMessage2.setId(2L);

        List<SupportMessage> supportMessages = new ArrayList<>(List.of(fakeSupportMessage1,fakeSupportMessage2));

        when(supportMessageRepository.findAll()).thenReturn(supportMessages);

        List<SupportDTO> result = supportMessageServiceImpl.getAllSupportMessages();

        assertNotNull(result);
        assertEquals(supportMessages.size(), result.size());
        verify(supportMessageRepository, times(1)).findAll();
    }

    @Test
    void search_ShouldReturnListSupportDTO() {
        SupportMessage fakeSupportMessage1 = new SupportMessage();
        fakeSupportMessage1.setId(1L);
        fakeSupportMessage1.setCreatedAt(LocalDateTime.now());
        fakeSupportMessage1.setUserEmail("test@email");

        SupportMessage fakeSupportMessage2 = new SupportMessage();
        fakeSupportMessage2.setId(2L);
        fakeSupportMessage2.setCreatedAt(LocalDateTime.now());
        fakeSupportMessage1.setUserEmail("test@email");

        List<SupportMessage> supportMessages = new ArrayList<>();
        supportMessages.add(fakeSupportMessage1);
        supportMessages.add(fakeSupportMessage2);

        LocalDate fakeDate = LocalDate.now();

        LocalDateTime startOfDay = fakeDate.atStartOfDay();
        LocalDateTime endOfDay = fakeDate.atTime(LocalTime.MAX);

        when(supportMessageRepository.search(fakeSupportMessage2.getUserEmail(),  startOfDay, endOfDay)).thenReturn(supportMessages);

        List<SupportDTO> result = supportMessageServiceImpl.search(fakeSupportMessage2.getUserEmail(), LocalDate.now());

        assertNotNull(result);
        assertEquals(supportMessages.size(), result.size());
        verify(supportMessageRepository, times(1)).search(fakeSupportMessage2.getUserEmail(), startOfDay, endOfDay);
    }

    @Test
    void markAsInProgress_WhenMessageIsPresent() {
        SupportMessage fakeSupportMessage = new SupportMessage();
        fakeSupportMessage.setId(1L);
        fakeSupportMessage.setStatusSupportMessage(StatusSupportMessage.NEW);

        when(supportMessageRepository.findSupportMessageById(1L)).thenReturn(Optional.of(fakeSupportMessage));
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(i -> i.getArgument(0));

        supportMessageServiceImpl.markAsInProgress(1L);

        ArgumentCaptor<SupportMessage> captor = ArgumentCaptor.forClass(SupportMessage.class);
        verify(supportMessageRepository, times(1)).save(captor.capture());

        SupportMessage saved = captor.getValue();

        assertEquals(StatusSupportMessage.IN_PROGRESS, saved.getStatusSupportMessage());
        verify(supportMessageRepository, times(1)).findSupportMessageById(1L);
    }

    @Test
    void markAsInProgress_WhenMessageIsNotPresent() {
        when(supportMessageRepository.findSupportMessageById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result =  assertThrows(ResponseStatusException.class,
                () -> supportMessageServiceImpl.markAsInProgress(1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Support Message not found", result.getReason());
    }

    @Test
    void markAsInCompleted_WhenMessageIsPresent() {
        SupportMessage fakeSupportMessage = new SupportMessage();
        fakeSupportMessage.setId(1L);
        fakeSupportMessage.setStatusSupportMessage(StatusSupportMessage.NEW);

        when(supportMessageRepository.findSupportMessageById(1L)).thenReturn(Optional.of(fakeSupportMessage));
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(i -> i.getArgument(0));

        supportMessageServiceImpl.markAsCompleted(1L);

        ArgumentCaptor<SupportMessage> captor = ArgumentCaptor.forClass(SupportMessage.class);
        verify(supportMessageRepository, times(1)).save(captor.capture());

        SupportMessage saved = captor.getValue();

        assertEquals(StatusSupportMessage.COMPLETED, saved.getStatusSupportMessage());
        verify(supportMessageRepository, times(1)).findSupportMessageById(1L);
    }

    @Test
    void markAsCompleted_WhenMessageIsNotPresent() {
        when(supportMessageRepository.findSupportMessageById(1L)).thenReturn(Optional.empty());

        ResponseStatusException result =  assertThrows(ResponseStatusException.class,
                () -> supportMessageServiceImpl.markAsCompleted(1L));

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Support Message not found", result.getReason());
    }




}
