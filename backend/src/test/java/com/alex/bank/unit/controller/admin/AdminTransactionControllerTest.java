package com.alex.bank.unit.controller.admin;

import com.alex.bank.controllers.admin.AdminTransactionController;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.mapper.TransactionMapperImpl;
import com.alex.bank.models.transaction.StatusTransaction;
import com.alex.bank.models.transaction.TypeTransaction;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.transaction.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminTransactionController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionMapperImpl.class)
public class AdminTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserTransactions_ShouldReturnTransactionDTOAdminList_WhenUserExists() throws Exception {
        TransactionDetailsDTO t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));
        TransactionDetailsDTO t2 = buildTransaction(2L, TypeTransaction.WITHDRAWAL, BigDecimal.valueOf(50));

        when(transactionService.getListUserTransactionForAdmin(eq(1L), any())).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(transactionService).getListUserTransactionForAdmin(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserTransactions_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(transactionService).getListUserTransactionForAdmin(eq(99L), any());

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransaction_ShouldReturnTransactionDTOAdmin_WhenTransactionExists() throws Exception {
        TransactionDetailsDTO fakeTransaction = buildTransaction(1L, TypeTransaction.TRANSFER, BigDecimal.valueOf(300));

        when(transactionService.getTransactionById(1L)).thenReturn(fakeTransaction);

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300));

        verify(transactionService).getTransactionById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransaction_ShouldReturn404_WhenTransactionDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"))
                .when(transactionService).getTransactionById(99L);

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/{transactionId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccountTransactions_ShouldReturnFilteredTransactions() throws Exception {
        TransactionDetailsDTO t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));

        when(transactionService.getListAccountTransactions(eq(1L), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/filter/{accountId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsByDate_ShouldReturnFilteredTransactions() throws Exception {
        TransactionDetailsDTO t1 = buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100));

        when(transactionService.getListAccountTransactionsInDate(eq(1L), any(), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/filter/date", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsByAmount_ShouldReturnFilteredTransactions() throws Exception {
        TransactionDetailsDTO t1 = buildTransaction(1L, TypeTransaction.WITHDRAWAL, BigDecimal.valueOf(50));

        when(transactionService.getListAccountTransactionsByAmount(eq(1L), any(), any())).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/filter/amount", 1L)
                        .param("amount", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsBeforeDate_ShouldReturnFilteredTransactions() throws Exception {
        when(transactionService.getListAccountTransactionsBeforeDate(eq(1L), any(), any()))
                .thenReturn(List.of(buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100))));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/filter/beforeDate", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsAfterDate_ShouldReturnFilteredTransactions() throws Exception {
        when(transactionService.getListAccountTransactionsAfterDate(eq(1L), any(), any()))
                .thenReturn(List.of(buildTransaction(1L, TypeTransaction.DEPOSIT, BigDecimal.valueOf(100))));

        mockMvc.perform(get("/api/v1/admin/{userId}/transactions/filter/afterDate", 1L)
                        .param("date", "01.01.2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTransaction_ShouldReturnCancelledTransactionDTOAdmin() throws Exception {
        TransactionDetailsDTO fakeDTO = new TransactionDetailsDTO();
        fakeDTO.setAmount(BigDecimal.valueOf(200));
        fakeDTO.setStatusTransaction(StatusTransaction.CANCELLED);

        when(transactionService.cancelTransaction(1L)).thenReturn(fakeDTO);

        mockMvc.perform(put("/api/v1/admin/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusTransaction").value("CANCELLED"));

        verify(transactionService).cancelTransaction(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTransaction_ShouldReturn400_WhenTransactionIsAlreadyCompleted() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't cancel this transaction, because it's completed"))
                .when(transactionService).cancelTransaction(1L);

        mockMvc.perform(put("/api/v1/admin/{userId}/transactions/{transactionId}", 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTransaction_ShouldReturn404_WhenTransactionDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction is not found"))
                .when(transactionService).cancelTransaction(99L);

        mockMvc.perform(put("/api/v1/admin/{userId}/transactions/{transactionId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    private TransactionDetailsDTO buildTransaction(Long id, TypeTransaction type, BigDecimal amount) {
        TransactionDetailsDTO t = new TransactionDetailsDTO();
        t.setId(id);
        t.setTypeTransaction(type);
        t.setStatusTransaction(StatusTransaction.SUCCESS);
        t.setAmount(amount);
        t.setAccountFrom("123456789/0001");
        t.setCreatedAt(LocalDateTime.now());
        t.setIsHidden(false);
        return t;
    }
}
