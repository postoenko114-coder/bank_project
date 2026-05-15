package com.example.demo.unit.service;

import com.example.demo.dto.CardDTO;
import com.example.demo.mapper.CardMapper;
import com.example.demo.mapper.CardMapperImpl;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.models.card.Card;
import com.example.demo.models.card.StatusCard;
import com.example.demo.models.card.TypeCard;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardServiceImpl;
import com.example.demo.services.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CardServiceImpl cardServiceImpl;

    @Spy
    private CardMapper cardMapper = new CardMapperImpl();

    @Test
    void createCard_ShouldReturnCardDTO_WhenAccountExistsAndHasNoCard() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("alex");

        Account fakeAccount = new Account();
        fakeAccount.setCard(null);
        fakeAccount.setUser(fakeUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDTO result = cardServiceImpl.createCard(1L, "DEBIT");

        assertNotNull(result);
        assertEquals(TypeCard.DEBIT, fakeAccount.getCard().getTypeCard());
        assertEquals(StatusCard.ACTIVE, fakeAccount.getCard().getStatusCard());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowConflict_WhenCardAlreadyExistsForAccount() {
        Card existingCard = new Card();

        Account fakeAccount = new Account();
        fakeAccount.setCard(existingCard);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.createCard(1L, "DEBIT"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card already exists for this account", exception.getReason());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.createCard(1L, "DEBIT"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    // ───── getListUserCards ─────

    @Test
    void getListUserCards_ShouldReturnCardDTOList_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Card card1 = new Card();
        card1.setTypeCard(TypeCard.DEBIT);
        card1.setStatusCard(StatusCard.ACTIVE);
        card1.setExpiryDate(LocalDate.now().plusYears(3));

        Card card2 = new Card();
        card2.setTypeCard(TypeCard.CREDIT);
        card2.setStatusCard(StatusCard.ACTIVE);
        card2.setExpiryDate(LocalDate.now().plusYears(2));

        fakeUser.setCards(List.of(card1, card2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

        List<CardDTO> result = cardServiceImpl.getListUserCards(1L);

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getListUserCards_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.getListUserCards(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    // ───── getCardById ─────

    @Test
    void getCardById_ShouldReturnCardDTO_WhenCardExists() {
        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.getCardById(1L);

        assertNotNull(result);
        verify(cardRepository, times(1)).findById(1L);
    }

    @Test
    void getCardById_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.getCardById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── findCardByNumber ─────

    @Test
    void findCardByNumber_ShouldReturnCardDTO_WhenCardExists() {
        Card fakeCard = new Card();
        fakeCard.setCardNumber("1234 5678 9012 3456");
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardRepository.findByCardNumber("1234 5678 9012 3456")).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.findCardByNumber("1234 5678 9012 3456");

        assertNotNull(result);
        verify(cardRepository, times(1)).findByCardNumber("1234 5678 9012 3456");
    }

    @Test
    void findCardByNumber_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findByCardNumber("0000 0000 0000 0000")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.findCardByNumber("0000 0000 0000 0000"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── blockCard ─────

    @Test
    void blockCard_ShouldReturnCardDTO_WhenCardIsActive() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));
        fakeCard.setCardNumber("1234 5678 9012 3456");
        fakeCard.setUser(fakeUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.blockCard(1L);

        assertNotNull(result);
        assertEquals(StatusCard.BLOCKED, fakeCard.getStatusCard());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void blockCard_ShouldThrowConflict_WhenCardAlreadyBlocked() {
        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.blockCard(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card already blocked", exception.getReason());
    }

    @Test
    void blockCard_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.blockCard(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── closeCard ─────

    @Test
    void closeCard_ShouldReturnCardDTO_WhenCardIsActive() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));
        fakeCard.setCardNumber("1234 5678 9012 3456");
        fakeCard.setUser(fakeUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.closeCard(1L);

        assertNotNull(result);
        assertEquals(StatusCard.CLOSED, fakeCard.getStatusCard());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void closeCard_ShouldThrowConflict_WhenCardAlreadyClosed() {
        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.CLOSED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.closeCard(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card already closed", exception.getReason());
    }

    @Test
    void closeCard_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.closeCard(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── activateCard ─────

    @Test
    void activateCard_ShouldReturnCardDTO_WhenCardIsBlocked() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.BLOCKED);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));
        fakeCard.setCardNumber("1234 5678 9012 3456");
        fakeCard.setUser(fakeUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.activateCard(1L);

        assertNotNull(result);
        assertEquals(StatusCard.ACTIVE, fakeCard.getStatusCard());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void activateCard_ShouldThrowConflict_WhenCardAlreadyActive() {
        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.activateCard(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card already active", exception.getReason());
    }

    @Test
    void activateCard_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.activateCard(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── changeTypeCard ─────

    @Test
    void changeTypeCard_ShouldChangeToCredit_WhenCardTypeIsDebit() {
        Card fakeCard = new Card();
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.changeTypeCard(1L);

        assertNotNull(result);
        assertEquals(TypeCard.CREDIT, fakeCard.getTypeCard());
    }

    @Test
    void changeTypeCard_ShouldChangeToDebit_WhenCardTypeIsCredit() {
        Card fakeCard = new Card();
        fakeCard.setTypeCard(TypeCard.CREDIT);
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        CardDTO result = cardServiceImpl.changeTypeCard(1L);

        assertNotNull(result);
        assertEquals(TypeCard.DEBIT, fakeCard.getTypeCard());
    }

    @Test
    void changeTypeCard_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.changeTypeCard(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    // ───── deleteCard ─────

    @Test
    void deleteCard_ShouldCallRepositoryDelete() {
        cardServiceImpl.deleteCard(1L);

        verify(cardRepository, times(1)).deleteById(1L);
    }

    // ───── payByCard ─────

    @Test
    void payByCard_ShouldCallAccountService_WhenCardExistsAndAccountIsActiveAndNotExpired() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));
        fakeCard.setAccount(fakeAccount);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        cardServiceImpl.payByCard(1L, BigDecimal.valueOf(100));

        verify(accountService, times(1)).payByCard(any(), eq(BigDecimal.valueOf(100)));
    }

    @Test
    void payByCard_ShouldThrowUnprocessableEntity_WhenAccountIsNotActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.BLOCKED);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));
        fakeCard.setAccount(fakeAccount);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.payByCard(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
    }

    @Test
    void payByCard_ShouldThrowUnprocessableEntity_WhenCardIsExpired() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);

        Card fakeCard = new Card();
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().minusDays(1));
        fakeCard.setAccount(fakeAccount);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fakeCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.payByCard(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals("Card is expired", exception.getReason());
    }

    @Test
    void payByCard_ShouldThrowNotFound_WhenCardDoesNotExist() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardServiceImpl.payByCard(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

}
