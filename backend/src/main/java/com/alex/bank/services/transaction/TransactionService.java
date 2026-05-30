package com.alex.bank.services.transaction;

import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.models.account.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    @Transactional
    List<TransactionDetailsDTO> getListAccountTransactions(Long account_id, Pageable pageable);

    @Transactional
    List<TransactionDetailsDTO> getListUserTransaction(Long user_id, Pageable pageable);

    @Transactional
    List<TransactionDetailsDTO> getListUserTransactionForAdmin(Long user_id, Pageable pageable);

    @Transactional
    TransactionDTO recordDeposit(Account account, BigDecimal amount);

    @Transactional
    TransactionDTO recordWithdrawal(Account account, BigDecimal amount);

    @Transactional
    TransactionDTO recordTransfer(Account accountTo, Account accountFrom, BigDecimal amount, String description);

    @Transactional
    TransactionDTO recordPaymentByCard(Account account, BigDecimal amount);

    @Transactional
    TransactionDetailsDTO getTransactionById(Long transaction_id);

    @Transactional
    List<TransactionDetailsDTO> getListAccountTransactionsByAmount(Long account_id, BigDecimal amount, Pageable pageable);

    @Transactional
    List<TransactionDetailsDTO> getListAccountTransactionsInDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    List<TransactionDetailsDTO> getListAccountTransactionsBeforeDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    List<TransactionDetailsDTO> getListAccountTransactionsAfterDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    TransactionDTO hideTransaction(Long transaction_id);

    @Transactional
    TransactionDetailsDTO cancelTransaction(Long transactionId);
}
