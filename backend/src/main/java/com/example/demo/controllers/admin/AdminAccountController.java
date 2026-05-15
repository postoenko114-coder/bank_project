package com.example.demo.controllers.admin;

import com.example.demo.dto.AccountDTO;
import com.example.demo.services.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId) {
        return accountService.getListUserAccounts(userId);
    }

    @GetMapping("/filter/number")
    public AccountDTO findByNumber(@RequestParam String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getAccount(@PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@PathVariable Long userId, @RequestParam String currency) {
        AccountDTO newAccountDTO = accountService.addAccount(userId, currency);
        return newAccountDTO;
    }

    @PutMapping("/{accountId}/closeAccount")
    public AccountDTO closeAccount(@PathVariable Long accountId) {
        return accountService.closeAccount(accountId);
    }

    @PutMapping("/{accountId}/blockAccount")
    public AccountDTO blockAccount(@PathVariable Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @PutMapping("/{accountId}/activateAccount")
    public AccountDTO activateAccount(@PathVariable Long accountId) {
       return accountService.activeAccount(accountId);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long accountId) {
        accountService.removeAccount(accountId);
    }

}
