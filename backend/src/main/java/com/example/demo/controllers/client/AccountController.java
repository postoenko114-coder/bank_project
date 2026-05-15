package com.example.demo.controllers.client;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.TransferDTO;
import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.security.IsOwner;
import com.example.demo.services.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/accounts")
@IsOwner
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final AccountMapper accountMapper;

    private final TransactionMapper transactionMapper;


    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId) {
        return accountService.getListUserAccounts(userId);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getUserAccount(@PathVariable Long userId, @PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createUserAccount(@PathVariable Long userId, @RequestParam String currency) {
        return accountService.addAccount(userId, currency);
    }

    @PostMapping("/{accountId}/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO makeWithdrawal(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Transaction transaction = accountService.withdrawal(accountId, amount);
        return transactionMapper.toDTO(transaction);
    }

    @PostMapping("/{accountId}/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO makeTransfer(@PathVariable Long userId, @PathVariable Long accountId, @RequestBody TransferDTO transferDTO) {
        Transaction transaction = accountService.transfer(accountId, accountService.getAccountByNumber(transferDTO.getAccountTo()).getId(), transferDTO.getAmount(), transferDTO.getDescription());
        return transactionMapper.toDTO(transaction);
    }

    @PostMapping("/{accountId}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO makeDeposit(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Transaction transaction = accountService.deposit(accountId, amount);
        return transactionMapper.toDTO(transaction);
    }

    @PostMapping("/{accountId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO payByCard(@PathVariable Long userId, @PathVariable Long accountId, @RequestParam BigDecimal amount) {
        Transaction transaction = accountService.payByCard(accountId, amount);
        return transactionMapper.toDTO(transaction);
    }

    @PutMapping("/{accountId}/closeAccount")
    public AccountDTO closeAccount(@PathVariable Long userId, @PathVariable Long accountId) {
        return accountService.closeAccount(accountId);
    }

}
