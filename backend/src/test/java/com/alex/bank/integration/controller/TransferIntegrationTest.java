package com.alex.bank.integration.controller;

import com.alex.bank.dto.TransferDTO;
import com.alex.bank.integration.config.IntegrationTestBase;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.security.UserSecurity;
import com.alex.bank.services.notification.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class TransferIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserSecurity userSecurity;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "login@gmail.com")
    void transfer_ShouldReturn201_WhenBothAccountsIsSufficient() throws Exception {
        when(userSecurity.isResourceOwner(any(), any())).thenReturn(true);
        when(userSecurity.isAccountOwner(any(), any())).thenReturn(true);

        Account accountTo = createAccount("1111111111", "100.00");
        Account accountFrom = createAccount("2222222222", "100.00");

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo(accountTo.getAccountNumber());
        transferDTO.setAmount(new BigDecimal("50.00"));

        doNothing().when(notificationService).notifyTransfer(any(), any(), any());

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, accountFrom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.accountTo").value(accountTo.getAccountNumber()))
                .andExpect(jsonPath("$.accountFrom").value(accountFrom.getAccountNumber()));

        Account accountToUpdate = accountRepository.findById(accountTo.getId()).orElseThrow();
        Account accountFromUpdate = accountRepository.findById(accountFrom.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("50.00").compareTo(accountFromUpdate.getBalance()));
        assertEquals(0, new BigDecimal("150.00").compareTo(accountToUpdate.getBalance()));
    }

    @Test
    @WithMockUser(username = "login@gmail.com")
    void transfer_ShouldReturn400_WhenInsufficientFunds() throws Exception {
        when(userSecurity.isResourceOwner(any(), any())).thenReturn(true);
        when(userSecurity.isAccountOwner(any(), any())).thenReturn(true);

        Account accountTo = createAccount("3333333333", "100.00");
        Account accountFrom = createAccount("4444444444", "50.00");

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo(accountTo.getAccountNumber());
        transferDTO.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, accountFrom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest());

        Account accountFromUpdate = accountRepository.findById(accountFrom.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(accountFromUpdate.getBalance()));
    }

    @Test
    @WithMockUser(username = "login@gmail.com")
    void transfer_ShouldReturn404_WhenAccountToNotFound() throws Exception {
        when(userSecurity.isResourceOwner(any(), any())).thenReturn(true);
        when(userSecurity.isAccountOwner(any(), any())).thenReturn(true);

        Account accountFrom = createAccount("5555555555", "100.00");

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo("9999999999");
        transferDTO.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, accountFrom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isNotFound());
    }

    private Account createAccount(String accountNumber, String balance) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal(balance));
        account.setStatusAccount(StatusAccount.ACTIVE);
        account.setCurrencyAccount(CurrencyAccount.CZK);
        account.setTransactionsFrom(new ArrayList<>());
        return accountRepository.save(account);
    }
}
