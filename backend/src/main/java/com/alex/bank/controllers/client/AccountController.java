package com.alex.bank.controllers.client;

import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.TransferDTO;
import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/accounts")
@IsOwner
@RequiredArgsConstructor
@Tag(name = "Client Accounts", description = "Client account operations and money movement endpoints")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Get all accounts of the authenticated user")
    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId) {
        return accountService.getListUserAccounts(userId);
    }

    @Operation(summary = "Get an account of the authenticated user by id")
    @GetMapping("/{accountId}")
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public AccountDTO getUserAccount(@PathVariable Long userId, @PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @Operation(summary = "Create a new account for the authenticated user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createUserAccount(@PathVariable Long userId, @RequestParam String currency) {
        return accountService.addAccount(userId, currency);
    }

    @Operation(summary = "Withdraw money from a user account")
    @PostMapping("/{accountId}/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public TransactionDTO makeWithdrawal(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.withdrawal(accountId, amount);
    }

    @Operation(summary = "Transfer money from a user account to another account")
    @PostMapping("/{accountId}/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public TransactionDTO makeTransfer(@PathVariable Long userId, @PathVariable Long accountId, @Valid @RequestBody TransferDTO transferDTO) {
        return accountService.transfer(accountId, accountService.getAccountByNumber(transferDTO.getAccountTo()).getId(), transferDTO.getAmount(), transferDTO.getDescription());
    }

    @Operation(summary = "Deposit money to a user account")
    @PostMapping("/{accountId}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public TransactionDTO makeDeposit(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.deposit(accountId, amount);
    }

    @Operation(summary = "Create a card payment transaction from a user account")
    @PostMapping("/{accountId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public TransactionDTO payByCard(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        return accountService.payByCard(accountId, amount);
    }

    @Operation(summary = "Close a user account")
    @PutMapping("/{accountId}/closeAccount")
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public AccountDTO closeAccount(@PathVariable Long userId, @PathVariable Long accountId) {
        return accountService.closeAccount(accountId);
    }

}
