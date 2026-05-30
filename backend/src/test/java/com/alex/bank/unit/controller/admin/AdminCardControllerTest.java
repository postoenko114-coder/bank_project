package com.alex.bank.unit.controller.admin;

import com.alex.bank.controllers.admin.AdminCardController;
import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.CardDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.models.card.StatusCard;
import com.alex.bank.models.card.TypeCard;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.account.AccountService;
import com.alex.bank.services.card.CardService;
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

@WebMvcTest(controllers = {AdminCardController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private CardDTO buildCardDTO(Long id, StatusCard status, TypeCard type) {
        CardDTO dto = new CardDTO();
        dto.setId(id);
        dto.setCardNumber("1234 5678 9012 000" + id);
        dto.setStatusCard(status);
        dto.setTypeCard(type);
        dto.setExpiryDate(LocalDate.now().plusYears(5));
        dto.setCardHolderName("Alex Smith");
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserCards_ShouldReturnCardDTOList_WhenUserExists() throws Exception {
        when(cardService.getListUserCards(1L))
                .thenReturn(List.of(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.DEBIT),
                        buildCardDTO(2L, StatusCard.ACTIVE, TypeCard.CREDIT)));

        mockMvc.perform(get("/api/v1/admin/{userId}/cards", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(cardService).getListUserCards(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserCard_ShouldReturnCardDTO_WhenCardExists() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.DEBIT));

        mockMvc.perform(get("/api/v1/admin/{userId}/cards/{cardId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCard").value("DEBIT"));

        verify(cardService).getCardById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserCard_ShouldReturn404_WhenCardDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"))
                .when(cardService).getCardById(99L);

        mockMvc.perform(get("/api/v1/admin/{userId}/cards/{cardId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserCardByNumber_ShouldReturnCardDTO_WhenCardExists() throws Exception {
        when(cardService.findCardByNumber("1234 5678 9012 3456"))
                .thenReturn(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.DEBIT));

        mockMvc.perform(get("/api/v1/admin/{userId}/cards/filter/number", 1L)
                        .param("cardNumber", "1234 5678 9012 3456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("1234 5678 9012 0001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserCard_ShouldReturnCardDTO_WhenDataIsValid() throws Exception {
        AccountDTO fakeAccount = new AccountDTO();
        fakeAccount.setId(1L);

        when(accountService.getAccountByNumber("123456789/0001")).thenReturn(fakeAccount);
        when(cardService.createCard(1L, "DEBIT")).thenReturn(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.DEBIT));

        mockMvc.perform(post("/api/v1/admin/{userId}/cards", 1L)
                        .param("accountNumber", "123456789/0001")
                        .param("typeCard", "DEBIT"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCard").value("ACTIVE"));

        verify(cardService).createCard(1L, "DEBIT");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUserCard_ShouldReturnBlockedCardDTO() throws Exception {
        when(cardService.blockCard(1L)).thenReturn(buildCardDTO(1L, StatusCard.BLOCKED, TypeCard.DEBIT));

        mockMvc.perform(put("/api/v1/admin/{userId}/cards/{cardId}/blockCard", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCard").value("BLOCKED"));

        verify(cardService).blockCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUserCard_ShouldReturnActiveCardDTO() throws Exception {
        when(cardService.activateCard(1L)).thenReturn(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.DEBIT));

        mockMvc.perform(put("/api/v1/admin/{userId}/cards/{cardId}/activateCard", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCard").value("ACTIVE"));

        verify(cardService).activateCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeTypeCard_ShouldReturnCardDTOWithNewType() throws Exception {
        when(cardService.changeTypeCard(1L)).thenReturn(buildCardDTO(1L, StatusCard.ACTIVE, TypeCard.CREDIT));

        mockMvc.perform(put("/api/v1/admin/{userId}/cards/{cardId}/changeTypeCard", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCard").value("CREDIT"));

        verify(cardService).changeTypeCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserCard_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/{userId}/cards/{cardId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }
}