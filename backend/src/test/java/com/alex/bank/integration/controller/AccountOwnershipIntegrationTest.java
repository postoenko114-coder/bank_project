package com.alex.bank.integration.controller;

import com.alex.bank.dto.TransferDTO;
import com.alex.bank.integration.config.IntegrationTestBase;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.models.user.RoleUser;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AccountOwnershipIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "owner.integration@gmail.com", roles = "CLIENT")
    void transfer_ShouldReturn403_WhenAccountBelongsToAnotherUser() throws Exception {
        User owner = createUser("owner.integration@gmail.com");
        User anotherUser = createUser("another.integration@gmail.com");

        Account anotherUserAccount = createAccount(anotherUser, "7000000001", "100.00");
        Account recipientAccount = createAccount(owner, "7000000002", "100.00");

        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setAccountTo(recipientAccount.getAccountNumber());
        transferDTO.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/{userId}/accounts/{accountId}/transfer", owner.getId(), anotherUserAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email) {
        User user = new User();
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRoleUser(RoleUser.CLIENT);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Account createAccount(User user, String accountNumber, String balance) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setBalance(new BigDecimal(balance));
        account.setStatusAccount(StatusAccount.ACTIVE);
        account.setCurrencyAccount(CurrencyAccount.CZK);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }
}
