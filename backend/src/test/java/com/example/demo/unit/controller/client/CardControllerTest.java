package com.example.demo.unit.controller.client;

import com.example.demo.controllers.client.CardController;
import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.CardDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.models.card.StatusCard;
import com.example.demo.models.card.TypeCard;
import com.example.demo.security.JwtService;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CardController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getCards_ShouldReturnCardDTOList_WhenUserExists() throws Exception {
        CardDTO card1 = new CardDTO();
        card1.setId(1L);
        card1.setTypeCard(TypeCard.DEBIT);
        card1.setStatusCard(StatusCard.ACTIVE);
        card1.setExpiryDate(LocalDate.now().plusYears(5));

        CardDTO card2 = new CardDTO();
        card2.setId(2L);
        card2.setTypeCard(TypeCard.CREDIT);
        card2.setStatusCard(StatusCard.ACTIVE);
        card2.setExpiryDate(LocalDate.now().plusYears(3));

        when(cardService.getListUserCards(1L)).thenReturn(List.of(card1, card2));

        mockMvc.perform(get("/api/v1/{userId}/cards", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(cardService).getListUserCards(1L);
    }

    @Test
    @WithMockUser
    void getCards_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(cardService).getListUserCards(99L);

        mockMvc.perform(get("/api/v1/{userId}/cards", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getCard_ShouldReturnCardDTO_WhenCardExists() throws Exception {
        CardDTO fakeCard = new CardDTO();
        fakeCard.setId(1L);
        fakeCard.setCardNumber("1234 5678 9012 3456");
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardService.getCardById(1L)).thenReturn(fakeCard);

        mockMvc.perform(get("/api/v1/{userId}/cards/{cardId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardNumber").value("1234 5678 9012 3456"));

        verify(cardService).getCardById(1L);
    }

    @Test
    @WithMockUser
    void getCard_ShouldReturn404_WhenCardDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"))
                .when(cardService).getCardById(99L);

        mockMvc.perform(get("/api/v1/{userId}/cards/{cardId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createCard_ShouldReturnCardDTO_WhenDataIsValid() throws Exception {
        AccountDTO fakeAccount = new AccountDTO();
        fakeAccount.setId(1L);

        CardDTO fakeCard = new CardDTO();
        fakeCard.setId(1L);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setStatusCard(StatusCard.ACTIVE);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(accountService.getAccountByNumber("123456789/0001")).thenReturn(fakeAccount);
        when(cardService.createCard(1L, "DEBIT")).thenReturn(fakeCard);

        mockMvc.perform(post("/api/v1/{userId}/cards", 1L)
                        .param("accountNumber", "123456789/0001")
                        .param("typeCard", "DEBIT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCard").value("DEBIT"));

        verify(cardService).createCard(1L, "DEBIT");
    }

    @Test
    @WithMockUser
    void createCard_ShouldReturn409_WhenCardAlreadyExists() throws Exception {
        AccountDTO fakeAccount = new AccountDTO();
        fakeAccount.setId(1L);

        when(accountService.getAccountByNumber("123456789/0001")).thenReturn(fakeAccount);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Card already exists for this account"))
                .when(cardService).createCard(1L, "DEBIT");

        mockMvc.perform(post("/api/v1/{userId}/cards", 1L)
                        .param("accountNumber", "123456789/0001")
                        .param("typeCard", "DEBIT"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void closeCard_ShouldReturnCardDTO_WhenCardExists() throws Exception {
        CardDTO fakeCard = new CardDTO();
        fakeCard.setId(1L);
        fakeCard.setStatusCard(StatusCard.CLOSED);
        fakeCard.setTypeCard(TypeCard.DEBIT);
        fakeCard.setExpiryDate(LocalDate.now().plusYears(5));

        when(cardService.closeCard(1L)).thenReturn(fakeCard);

        mockMvc.perform(put("/api/v1/{userId}/cards/{cardId}/closeCard", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCard").value("CLOSED"));

        verify(cardService).closeCard(1L);
    }

    @Test
    @WithMockUser
    void closeCard_ShouldReturn404_WhenCardDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"))
                .when(cardService).closeCard(99L);

        mockMvc.perform(put("/api/v1/{userId}/cards/{cardId}/closeCard", 1L, 99L))
                .andExpect(status().isNotFound());
    }
}