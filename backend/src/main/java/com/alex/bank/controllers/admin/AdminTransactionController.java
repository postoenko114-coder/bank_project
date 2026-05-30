package com.alex.bank.controllers.admin;

import com.alex.bank.dto.transaction.TransactionDTOAdmin;
import com.alex.bank.mapper.TransactionMapper;
import com.alex.bank.services.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/transactions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Transactions", description = "Administrative transaction monitoring endpoints")
public class AdminTransactionController {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    @Operation(summary = "Get all transactions of a user as admin")
    @GetMapping
    public List<TransactionDTOAdmin> getUserTransactions(@PathVariable Long userId, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListUserTransactionForAdmin(userId, pageable));
    }

    @Operation(summary = "Get all transactions of an account as admin")
    @GetMapping("/filter/{accountId}")
    public List<TransactionDTOAdmin> getAccountTransactions(@PathVariable Long accountId, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListAccountTransactions(accountId, pageable));
    }

    @Operation(summary = "Get a transaction by id as admin")
    @GetMapping("/{transactionId}")
    public TransactionDTOAdmin getTransaction(@PathVariable Long transactionId) {
        return transactionMapper.toDTOAdmin(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Get user transactions by exact date as admin")
    @GetMapping("/filter/date")
    public List<TransactionDTOAdmin> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListAccountTransactionsInDate(userId, date, pageable));
    }

    @Operation(summary = "Get user transactions by amount as admin")
    @GetMapping("/filter/amount")
    public List<TransactionDTOAdmin> getTransactionsByAmount(@PathVariable Long userId, @RequestParam BigDecimal amount, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListAccountTransactionsByAmount(userId, amount, pageable));
    }

    @Operation(summary = "Get user transactions before a date as admin")
    @GetMapping("/filter/beforeDate")
    public List<TransactionDTOAdmin> getTransactionsBeforeDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListAccountTransactionsBeforeDate(userId, date, pageable));
    }

    @Operation(summary = "Get user transactions after a date as admin")
    @GetMapping("/filter/afterDate")
    public List<TransactionDTOAdmin> getTransactionsAfterDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return transactionMapper.mapDetailsListToDTOAdmin(transactionService.getListAccountTransactionsAfterDate(userId, date, pageable));
    }

    @Operation(summary = "Cancel a transaction as admin")
    @PutMapping("/{transactionId}")
    public TransactionDTOAdmin cancelTransaction(@PathVariable Long transactionId) {
        return transactionMapper.toDTOAdmin(transactionService.cancelTransaction(transactionId));
    }

}
