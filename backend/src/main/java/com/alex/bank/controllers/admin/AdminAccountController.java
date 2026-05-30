package com.alex.bank.controllers.admin;

import com.alex.bank.dto.AccountDTO;
import com.alex.bank.services.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Accounts", description = "Administrative account management endpoints")
public class AdminAccountController {

    private final AccountService accountService;

    @Operation(summary = "Get all accounts of a user as admin")
    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId) {
        return accountService.getListUserAccounts(userId);
    }

    @Operation(summary = "Find an account by account number as admin")
    @GetMapping("/filter/number")
    public AccountDTO findByNumber(@RequestParam String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @Operation(summary = "Get an account by id as admin")
    @GetMapping("/{accountId}")
    public AccountDTO getAccount(@PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @Operation(summary = "Create an account for a user as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@PathVariable Long userId, @RequestParam String currency) {return accountService.addAccount(userId, currency);}

    @Operation(summary = "Close an account as admin")
    @PutMapping("/{accountId}/closeAccount")
    public AccountDTO closeAccount(@PathVariable Long accountId) {
        return accountService.closeAccount(accountId);
    }

    @Operation(summary = "Block an account as admin")
    @PutMapping("/{accountId}/blockAccount")
    public AccountDTO blockAccount(@PathVariable Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @Operation(summary = "Activate an account as admin")
    @PutMapping("/{accountId}/activateAccount")
    public AccountDTO activateAccount(@PathVariable Long accountId) {
       return accountService.activeAccount(accountId);
    }

    @Operation(summary = "Delete an account as admin")
    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long accountId) {
        accountService.removeAccount(accountId);
    }

}
