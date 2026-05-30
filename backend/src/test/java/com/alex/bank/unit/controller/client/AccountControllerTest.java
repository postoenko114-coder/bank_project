package com.alex.bank.unit.controller.client;


import com.alex.bank.controllers.client.AccountController;
import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.TransferDTO;
import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.exception.GlobalExceptionHandler;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.models.user.User;
import com.alex.bank.security.JwtService;
import com.alex.bank.services.account.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AccountService accountService;

    @Test
    @WithMockUser
    void getUserAccounts_ShouldReturnUserDTOAccounts_WhenUserExists() throws Exception {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setPassword("fakePassword");

        List<AccountDTO> fakeAccountDTOs = new ArrayList<>(List.of(new AccountDTO(), new AccountDTO()));

        when(accountService.getListUserAccounts(fakeUser.getId())).thenReturn(fakeAccountDTOs);

        mockMvc.perform(get("/api/v1/{userId}/accounts", fakeUser.getId()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(accountService).getListUserAccounts(fakeUser.getId());
    }

    @Test
    @WithMockUser
    void getUserAccounts_ShouldThrowsException_WhenUserDoesntExists() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(accountService).getListUserAccounts(anyLong());

        mockMvc.perform(get("/api/v1/{userId}/accounts", 1L))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser
    void getUserAccount_ShouldReturnAccountDTO_WhenUserDoesntExists() throws Exception {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("fakeUser");
        fakeUser.setPassword("fakePassword");

        AccountDTO  fakeAccountDTO = new AccountDTO();
        fakeAccountDTO.setId(1L);
        fakeAccountDTO.setAccountNumber("1111111");

        when(accountService.getAccountById(fakeUser.getId())).thenReturn(fakeAccountDTO);

        mockMvc.perform(get("/api/v1/{userId}/accounts/{accountId}", fakeUser.getId(),  fakeAccountDTO.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1111111"));

        verify(accountService).getAccountById(fakeUser.getId());
    }

    @Test
    @WithMockUser
    void getUserAccount_ShouldThrowsException_WhenUserDoesntExists() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(accountService).getAccountById(anyLong());

        mockMvc.perform(get("/api/v1/{userId}/accounts/{accountId}", 1L, 1L))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser
    void createUserAccount_ShouldReturnAccountDTO_WhenUserExists() throws Exception {
        User fakeUser = new User();
        fakeUser.setId(1L);

        String fakeCurrency = "CZK";

        AccountDTO fakeAccountDTO = new AccountDTO();
        fakeAccountDTO.setId(1L);
        fakeAccountDTO.setCurrency(CurrencyAccount.valueOf(fakeCurrency));

        when(accountService.addAccount(fakeUser.getId(),  fakeCurrency)).thenReturn(fakeAccountDTO);

        mockMvc.perform(post("/api/v1/{userId}/accounts", fakeUser.getId())
                .param("currency", fakeCurrency))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.currency").value(fakeCurrency));

        verify(accountService).addAccount(fakeUser.getId(), fakeCurrency);
    }

    @Test
    @WithMockUser
    void createUserAccount_ShouldThrowException_WhenUserDoesntExists() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(accountService).addAccount(anyLong(), any(String.class));

        mockMvc.perform(post("/api/v1/{userId}/accounts", 1L)
                        .param("currency" , "CZK"))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser
    void createUserAccount_ShouldReturn400_WhenCurrencyParamIsMissing() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/{userId}/accounts", userId))

                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void makeWithdrawal_ShouldReturnTransactionDTO_WhenAccountExists() throws Exception {
        AccountDTO fakeAccountDTO = new AccountDTO();
        fakeAccountDTO.setId(1L);
        fakeAccountDTO.setBalance(BigDecimal.valueOf(200));

        TransactionDTO fakeTransaction = new TransactionDTO();
        fakeTransaction.setId(1L);
        fakeTransaction.setAmount(BigDecimal.valueOf(100));

        BigDecimal fakeAmount = new BigDecimal("100");

        when(accountService.withdrawal(fakeAccountDTO.getId(), fakeAmount)).thenReturn(fakeTransaction);

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/withdrawal",1,  fakeAccountDTO.getId())
                .param("amount", String.valueOf(BigDecimal.valueOf(100))))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(fakeAmount));

        verify(accountService).withdrawal(fakeAccountDTO.getId(), fakeAmount);
    }

    @Test
    @WithMockUser
    void makeDeposit_ShouldReturnTransactionDTO_WhenAccountExists() throws Exception {
        TransactionDTO fakeTransaction = new TransactionDTO();
        fakeTransaction.setId(1L);
        fakeTransaction.setAmount(BigDecimal.valueOf(200));

        when(accountService.deposit(1L, BigDecimal.valueOf(200))).thenReturn(fakeTransaction);

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/deposit", 1L, 1L)
                        .param("amount", "200"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(200));

        verify(accountService).deposit(1L, BigDecimal.valueOf(200));
    }

    @Test
    @WithMockUser
    void makeDeposit_ShouldReturn404_WhenAccountNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).deposit(eq(99L), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/deposit", 1L, 99L)
                        .param("amount", "100"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void makeDeposit_ShouldReturn409_WhenAccountIsNotActive() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active"))
                .when(accountService).deposit(eq(1L), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/deposit", 1L, 1L)
                        .param("amount", "100"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void makeDeposit_ShouldReturn400_WhenAmountParamMissing() throws Exception {
        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/deposit", 1L, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void makeTransfer_ShouldReturnTransactionDTO_WhenBothAccountsExist() throws Exception {
        TransferDTO fakeTransferDTO = new TransferDTO();
        fakeTransferDTO.setAccountTo("123456789/0002");
        fakeTransferDTO.setAmount(BigDecimal.valueOf(300));
        fakeTransferDTO.setDescription("Test transfer");

        AccountDTO fakeAccountTo = new AccountDTO();
        fakeAccountTo.setId(2L);

        TransactionDTO fakeTransaction = new TransactionDTO();
        fakeTransaction.setId(1L);
        fakeTransaction.setAmount(BigDecimal.valueOf(300));

        when(accountService.getAccountByNumber("123456789/0002")).thenReturn(fakeAccountTo);
        when(accountService.transfer(1L, 2L, BigDecimal.valueOf(300), "Test transfer")).thenReturn(fakeTransaction);

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeTransferDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(300));

        verify(accountService).transfer(1L, 2L, BigDecimal.valueOf(300), "Test transfer");
    }

    @Test
    @WithMockUser
    void makeTransfer_ShouldReturn400_WhenInsufficientFunds() throws Exception {
        TransferDTO fakeTransferDTO = new TransferDTO();
        fakeTransferDTO.setAccountTo("123456789/0002");
        fakeTransferDTO.setAmount(BigDecimal.valueOf(9999));
        fakeTransferDTO.setDescription("Test");

        AccountDTO fakeAccountTo = new AccountDTO();
        fakeAccountTo.setId(2L);

        when(accountService.getAccountByNumber("123456789/0002")).thenReturn(fakeAccountTo);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds"))
                .when(accountService).transfer(anyLong(), anyLong(), any(), any());

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeTransferDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void makeTransfer_ShouldReturn404_WhenAccountNotFound() throws Exception {
        TransferDTO fakeTransferDTO = new TransferDTO();
        fakeTransferDTO.setAccountTo("000000000/0000");
        fakeTransferDTO.setAmount(BigDecimal.valueOf(100));
        fakeTransferDTO.setDescription("Test");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).getAccountByNumber("000000000/0000");

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeTransferDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void makePayment_ShouldReturnTransactionDTO_WhenAccountExists() throws Exception {
        TransactionDTO fakeTransaction = new TransactionDTO();
        fakeTransaction.setId(1L);
        fakeTransaction.setAmount(BigDecimal.valueOf(50));

        when(accountService.payByCard(1L, BigDecimal.valueOf(50))).thenReturn(fakeTransaction);

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/payment", 1L, 1L)
                        .param("amount", "50"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50));

        verify(accountService).payByCard(1L, BigDecimal.valueOf(50));
    }

    @Test
    @WithMockUser
    void makePayment_ShouldReturn400_WhenInsufficientFunds() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds"))
                .when(accountService).payByCard(eq(1L), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/payment", 1L, 1L)
                        .param("amount", "99999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void makePayment_ShouldReturn404_WhenAccountNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).payByCard(eq(99L), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/payment", 1L, 99L)
                        .param("amount", "100"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void closeAccount_ShouldReturnAccountDTO_WhenAccountIsActive() throws Exception {
        AccountDTO fakeAccountDTO = new AccountDTO();
        fakeAccountDTO.setId(1L);
        fakeAccountDTO.setStatusAccount(StatusAccount.CLOSED);
        fakeAccountDTO.setCurrency(CurrencyAccount.USD);

        when(accountService.closeAccount(1L)).thenReturn(fakeAccountDTO);

        mockMvc.perform(put("/api/v1/{userId}/accounts/{accountId}/closeAccount", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAccount").value("CLOSED"));

        verify(accountService).closeAccount(1L);
    }

    @Test
    @WithMockUser
    void closeAccount_ShouldReturn409_WhenAccountAlreadyClosed() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Account is already closed"))
                .when(accountService).closeAccount(1L);

        mockMvc.perform(put("/api/v1/{userId}/accounts/{accountId}/closeAccount", 1L, 1L))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void closeAccount_ShouldReturn404_WhenAccountNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).closeAccount(99L);

        mockMvc.perform(put("/api/v1/{userId}/accounts/{accountId}/closeAccount", 1L, 99L))
                .andExpect(status().isNotFound());
    }

}
