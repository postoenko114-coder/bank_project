package com.example.demo.integration.controller;

import com.example.demo.dto.TransferDTO;
import com.example.demo.integration.config.IntegrationTestBase;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.security.UserSecurity;
import com.example.demo.services.notification.NotificationService;
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

        Account accountTo = new Account();
        Account accountFrom = new Account();

        accountTo.setAccountNumber("1111111111");
        accountFrom.setAccountNumber("2222222222");

        accountTo.setBalance(new BigDecimal("100.00"));
        accountFrom.setBalance(new BigDecimal("100.00"));

        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountFrom.setStatusAccount(StatusAccount.ACTIVE);

        accountTo.setCurrencyAccount(CurrencyAccount.CZK);
        accountFrom.setCurrencyAccount(CurrencyAccount.CZK);

        accountTo.setTransactionsFrom(new ArrayList<>());
        accountFrom.setTransactionsFrom(new ArrayList<>());

        accountTo = accountRepository.save(accountTo);
        accountFrom = accountRepository.save(accountFrom);

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo(accountTo.getAccountNumber());
        transferDTO.setAmount(new BigDecimal("50.00"));

        doNothing().when(notificationService).notifyTransfer(any(), any(), any());

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, accountFrom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50)) // Иногда нужно .value(50.0)
                .andExpect(jsonPath("$.accountTo").value(accountTo.getAccountNumber()))
                .andExpect(jsonPath("$.accountFrom").value(accountFrom.getAccountNumber()));

        // 4. Проверяем реальное списание в базе данных
        Account accountToUpdate = accountRepository.findById(accountTo.getId()).orElseThrow();
        Account accountFromUpdate = accountRepository.findById(accountFrom.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("50.00").compareTo(accountFromUpdate.getBalance()));
        assertEquals(0, new BigDecimal("150.00").compareTo(accountToUpdate.getBalance()));
    }

    @Test
    @WithMockUser(username = "login@gmail.com")
    void transfer_ShouldReturn400_WhenInsufficientFunds() throws Exception {
        when(userSecurity.isResourceOwner(any(), any())).thenReturn(true);

        Account accountTo = new Account();
        accountTo.setAccountNumber("3333333333");
        accountTo.setBalance(new BigDecimal("100.00"));
        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountTo.setCurrencyAccount(CurrencyAccount.CZK);

        Account accountFrom = new Account();
        accountFrom.setAccountNumber("4444444444");
        accountFrom.setBalance(new BigDecimal("50.00"));
        accountFrom.setStatusAccount(StatusAccount.ACTIVE);
        accountFrom.setCurrencyAccount(CurrencyAccount.CZK);

        accountTo = accountRepository.save(accountTo);
        accountFrom = accountRepository.save(accountFrom);

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

        Account accountFrom = new Account();
        accountFrom.setAccountNumber("5555555555");
        accountFrom.setBalance(new BigDecimal("100.00"));
        accountFrom.setCurrencyAccount(CurrencyAccount.CZK);
        accountFrom = accountRepository.save(accountFrom);

        // 2. Указываем несуществующий счет получателя
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo("9999999999"); // Такого счета нет в БД
        transferDTO.setAmount(new BigDecimal("10.00"));

        // 3. Выполняем запрос
        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, accountFrom.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))

                // Ожидаем ошибку 404 Not Found
                .andExpect(status().isNotFound());
    }

}
