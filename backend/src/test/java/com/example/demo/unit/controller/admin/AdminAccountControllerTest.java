package com.example.demo.unit.controller.admin;

import com.example.demo.controllers.admin.AdminAccountController;
import com.example.demo.dto.AccountDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.mapper.AccountMapperImpl;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.security.JwtService;
import com.example.demo.services.account.AccountService;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminAccountController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(AccountMapperImpl.class)
public class AdminAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private AccountDTO buildAccountDTO(Long id, StatusAccount status) {
        AccountDTO dto = new AccountDTO();
        dto.setId(id);
        dto.setAccountNumber("123456789/000" + id);
        dto.setCurrency(CurrencyAccount.USD);
        dto.setBalance(BigDecimal.valueOf(1000));
        dto.setStatusAccount(status);
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserAccounts_ShouldReturnAccountDTOList_WhenUserExists() throws Exception {
        when(accountService.getListUserAccounts(1L))
                .thenReturn(List.of(buildAccountDTO(1L, StatusAccount.ACTIVE), buildAccountDTO(2L, StatusAccount.ACTIVE)));

        mockMvc.perform(get("/api/v1/admin/{userId}/accounts", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(accountService).getListUserAccounts(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserAccounts_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(accountService).getListUserAccounts(99L);

        mockMvc.perform(get("/api/v1/admin/{userId}/accounts", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccount_ShouldReturnAccountDTO_WhenAccountExists() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(buildAccountDTO(1L, StatusAccount.ACTIVE));

        mockMvc.perform(get("/api/v1/admin/{userId}/accounts/{accountId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAccount").value("ACTIVE"));

        verify(accountService).getAccountById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccount_ShouldReturn404_WhenAccountDoesNotExist() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).getAccountById(99L);

        mockMvc.perform(get("/api/v1/admin/{userId}/accounts/{accountId}", 1L, 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findByNumber_ShouldReturnAccountDTO_WhenAccountExists() throws Exception {
        when(accountService.getAccountByNumber("123456789/0001")).thenReturn(buildAccountDTO(1L, StatusAccount.ACTIVE));

        mockMvc.perform(get("/api/v1/admin/{userId}/accounts/filter/number", 1L)
                        .param("accountNumber", "123456789/0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123456789/0001"));

        verify(accountService).getAccountByNumber("123456789/0001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_ShouldReturnAccountDTO_WhenDataIsValid() throws Exception {
        when(accountService.addAccount(1L, "USD")).thenReturn(buildAccountDTO(1L, StatusAccount.ACTIVE));

        mockMvc.perform(post("/api/v1/admin/{userId}/accounts", 1L)
                        .param("currency", "USD"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(accountService).addAccount(1L, "USD");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeAccount_ShouldReturnClosedAccountDTO() throws Exception {
        when(accountService.closeAccount(1L)).thenReturn(buildAccountDTO(1L, StatusAccount.CLOSED));

        mockMvc.perform(put("/api/v1/admin/{userId}/accounts/{accountId}/closeAccount", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAccount").value("CLOSED"));

        verify(accountService).closeAccount(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockAccount_ShouldReturnBlockedAccountDTO() throws Exception {
        when(accountService.blockAccount(1L)).thenReturn(buildAccountDTO(1L, StatusAccount.BLOCKED));

        mockMvc.perform(put("/api/v1/admin/{userId}/accounts/{accountId}/blockAccount", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAccount").value("BLOCKED"));

        verify(accountService).blockAccount(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateAccount_ShouldReturnActiveAccountDTO() throws Exception {
        when(accountService.activeAccount(1L)).thenReturn(buildAccountDTO(1L, StatusAccount.ACTIVE));

        mockMvc.perform(put("/api/v1/admin/{userId}/accounts/{accountId}/activateAccount", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusAccount").value("ACTIVE"));

        verify(accountService).activeAccount(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAccount_ShouldReturn204_WhenAccountExists() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/{userId}/accounts/{accountId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(accountService).removeAccount(1L);
    }
}
