package com.example.demo.controllers.admin;

import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.security.IsOwner;
import com.example.demo.services.transaction.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/transactions")
@PreAuthorize("hasRole('ADMIN')")
@IsOwner
public class AdminTransactionController {

    private final TransactionService transactionService;

    public AdminTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDTOAdmin> getUserTransactions(@PathVariable Long userId, @PageableDefault(size = 10) Pageable pageable){
        return getTransactionDTOAdmins(transactionService.getListUserTransactionForAdmin(userId, pageable));
    }

    @GetMapping("/filter/{accountId}")
    public List<TransactionDTOAdmin> getAccountTransactions(@PathVariable Long accountId, @PageableDefault(size = 10) Pageable pageable){
        return getTransactionDTOAdmins(transactionService.getListAccountTransactions(accountId,  pageable));
    }

    @GetMapping("/{transactionId}")
    public TransactionDTOAdmin getTransaction(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId).toDTOAdmin();
    }

    @GetMapping("/filter/date")
    public List<TransactionDTOAdmin> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsInDate(userId, date,  pageable));
    }

    @GetMapping("/filter/amount")
    public List<TransactionDTOAdmin> getTransactionsByAmount(@PathVariable Long userId,  @RequestParam BigDecimal amount, @PageableDefault(size = 10) Pageable pageable) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsByAmount(userId, amount, pageable));
    }

    @GetMapping("/filter/beforeDate")
    public List<TransactionDTOAdmin> getTransactionsBeforeDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsBeforeDate(userId, date, pageable));
    }

    @GetMapping("/filter/afterDate")
    public List<TransactionDTOAdmin> getTransactionsAfterDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @PageableDefault(size = 10) Pageable pageable) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsAfterDate(userId, date,pageable));
    }

    @PutMapping("/{transactionId}")
    public TransactionDTOAdmin cancelTransaction(@PathVariable Long transactionId) {
        TransactionDTOAdmin transactionDTOAdmin = transactionService.cancelTransaction(transactionId);
        return transactionDTOAdmin ;
    }

    private List<TransactionDTOAdmin> getTransactionDTOAdmins(List<Transaction> transactions){
        List<TransactionDTOAdmin> transactionDTOAdmins = new ArrayList<>();
        for(Transaction transaction : transactions){
            transactionDTOAdmins.add(transaction.toDTOAdmin());
        }
        return transactionDTOAdmins;
    }
}
