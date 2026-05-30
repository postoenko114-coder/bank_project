package com.alex.bank.controllers.client;

import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.mapper.TransactionMapper;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/transactions")
@IsOwner
@RequiredArgsConstructor
@Tag(name = "Client Transactions", description = "Client transaction history endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    private final TransactionMapper transactionMapper;

    @Operation(summary = "Get all visible transactions of the authenticated user")
    @GetMapping
    public List<TransactionDTO> getUserTransactions(@PathVariable Long userId,  Pageable pageable) {
        return transactionMapper.mapDetailsListToDTO(transactionService.getListUserTransaction(userId, pageable));
    }

    @Operation(summary = "Get all visible transactions of a user account")
    @GetMapping("/filter/{accountId}")
    @PreAuthorize("@userSecurity.isAccountOwner(#userId, #accountId) or hasRole('ADMIN')")
    public List<TransactionDTO> getAccountTransactions(@PathVariable Long userId, @PathVariable Long accountId, Pageable pageable) {
        return transactionMapper.mapDetailsListToDTO(transactionService.getListAccountTransactions(accountId, pageable));
    }

    @Operation(summary = "Get a visible transaction by id")
    @GetMapping("/{transactionId}")
    @PreAuthorize("@userSecurity.isTransactionOwner(#userId, #transactionId) or hasRole('ADMIN')")
    public TransactionDTO getTransaction(@PathVariable Long userId, @PathVariable Long transactionId) {
        TransactionDetailsDTO transactionDetailsDTO = transactionService.getTransactionById(transactionId);
        if (transactionDetailsDTO.getIsHidden()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction is hidden");
        }
        return transactionMapper.toDTO(transactionDetailsDTO);
    }

    @Operation(summary = "Get visible user transactions by exact date")
    @GetMapping("/filter/date")
    public List<TransactionDTO> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, Pageable pageable) {
        return transactionMapper.mapDetailsListToDTO(transactionService.getListAccountTransactionsInDate(userId, date, pageable));
    }

    @Operation(summary = "Get visible user transactions after a date")
    @GetMapping("/filter/afterDate")
    public List<TransactionDTO> getTransactionAfterDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, Pageable pageable) {
        return transactionMapper.mapDetailsListToDTO(transactionService.getListAccountTransactionsAfterDate(userId, date, pageable));
    }

    @Operation(summary = "Get visible user transactions before a date")
    @GetMapping("/filter/beforeDate")
    public List<TransactionDTO> getTransactionBeforeDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, Pageable pageable) {
        return transactionMapper.mapDetailsListToDTO(transactionService.getListAccountTransactionsBeforeDate(userId, date, pageable));
    }

    @Operation(summary = "Get visible user transactions by amount")
    @GetMapping("/filter/amount")
    public List<TransactionDTO> getTransactionsByAmount(@PathVariable Long userId, @RequestParam BigDecimal amount, Pageable pageable) {
         return transactionMapper.mapDetailsListToDTO(transactionService.getListAccountTransactionsByAmount(userId, amount, pageable));
    }

    @Operation(summary = "Hide a transaction from the client history")
    @PutMapping("/{transactionId}")
    @PreAuthorize("@userSecurity.isTransactionOwner(#userId, #transactionId) or hasRole('ADMIN')")
    public TransactionDTO hideTransaction(@PathVariable Long userId, @PathVariable Long transactionId) {
        return transactionService.hideTransaction(transactionId);
    }


}
