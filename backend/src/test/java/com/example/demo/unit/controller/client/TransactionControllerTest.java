package com.example.demo.unit.controller.client;

import com.example.demo.controllers.client.TransactionController;
import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.transaction.TypeTransaction;
import com.example.demo.security.JwtService;
import com.example.demo.services.transaction.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TransactionController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Transaction buildTransaction(Long id, TypeTransaction type, BigDecimal amount) {
        Account fakeAccount = new Account();
        fakeAccount.setAccountNumber("123456789/0001");
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);

        Transaction t = new Transaction();
        t.setId(id);
        t.setTypeTransaction(type);
        t.setStatusTransaction(StatusTransaction.SUCCESS);
        t.setAmount(amount);
        t.setAccountFrom(fakeAccount);
        t.setCreatedAt(LocalDateTime.now());
        t.setHidden(false);
        return t;
    }

    @Test
    @WithMockUser
    void getUserTransactions_ShouldReturnTransactionDTOList_WhenUserExists() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));
        Transaction t2 = buildTransaction(2L, TypeTransaction.WITHDRAWAL, BigDecimal.valueOf(50));

        when(transactionService.getListUserTransaction(eq(1L), any())).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/v1/{userId}/transactions", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(transactionService).getListUserTransaction(eq(1L), any());
    }

    @Test
    @WithMockUser
    void getUserTransactions_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(transactionService).getListUserTransaction(eq(99L), any());

        mockMvc.perform(get("/api/v1/{userId}/transactions", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturnTransactionDTO_WhenTransactionExists() throws Exception {
        Transaction fakeTransaction = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(200));

        when(transactionService.getTransactionById(1L)).thenReturn(fakeTransaction);

        mockMvc.perform(get("/api/v1/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200));

        verify(transactionService).getTransactionById(1L);
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturn400_WhenTransactionIsHidden() throws Exception {
        Transaction hiddenTransaction = buildTransaction(1L, TypeTransaction.WITHDRAWAL, BigDecimal.valueOf(100));
        hiddenTransaction.setHidden(true);

        when(transactionService.getTransactionById(1L)).thenReturn(hiddenTransaction);

        mockMvc.perform(get("/api/v1/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturn404_WhenTransactionDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"))
                .when(transactionService).getTransactionById(99L);

        mockMvc.perform(get("/api/v1/{userId}/transactions/{transactionId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getAccountTransactions_ShouldReturnTransactionDTOList_WhenAccountExists() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));

        when(transactionService.getListAccountTransactions(eq(1L), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/{userId}/transactions/filter/{accountId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(transactionService).getListAccountTransactions(eq(1L), any());
    }

    @Test
    @WithMockUser
    void getTransactionsByDate_ShouldReturnFilteredTransactions() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));

        when(transactionService.getListAccountTransactionsInDate(eq(1L), any(), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/{userId}/transactions/filter/date", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(transactionService).getListAccountTransactionsInDate(eq(1L), any(), any());
    }

    @Test
    @WithMockUser
    void getTransactionsAfterDate_ShouldReturnFilteredTransactions() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.WITHDRAWAL, BigDecimal.valueOf(50));

        when(transactionService.getListAccountTransactionsAfterDate(eq(1L), any(), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/{userId}/transactions/filter/afterDate", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    void getTransactionsBeforeDate_ShouldReturnFilteredTransactions() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.TRANSFER, BigDecimal.valueOf(300));

        when(transactionService.getListAccountTransactionsBeforeDate(eq(1L), any(), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/{userId}/transactions/filter/beforeDate", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    void getTransactionsByAmount_ShouldReturnFilteredTransactions() throws Exception {
        Transaction t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));

        when(transactionService.getListAccountTransactionsByAmount(eq(1L), eq(BigDecimal.valueOf(100)), any()))
                .thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/{userId}/transactions/filter/amount", 1L)
                        .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser
    void hideTransaction_ShouldReturnTransactionDTO_WhenTransactionExists() throws Exception {
        TransactionDTO fakeDTO = new TransactionDTO();
        fakeDTO.setAmount(BigDecimal.valueOf(100));

        when(transactionService.hideTransaction(1L)).thenReturn(fakeDTO);

        mockMvc.perform(put("/api/v1/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isOk());

        verify(transactionService).hideTransaction(1L);
    }

    @Test
    @WithMockUser
    void hideTransaction_ShouldReturn404_WhenTransactionDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"))
                .when(transactionService).hideTransaction(99L);

        mockMvc.perform(put("/api/v1/{userId}/transactions/{transactionId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }
}