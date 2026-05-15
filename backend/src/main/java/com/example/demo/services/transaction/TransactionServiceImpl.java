package com.example.demo.services.transaction;


import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.models.account.Account;
import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.transaction.TypeTransaction;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactions(Long account_id, Pageable pageable) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(account.getUser().getId(), pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListUserTransaction(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(user_id, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListUserTransactionForAdmin(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllByUserId(user_id, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public Transaction recordDeposit(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.DEPOSIT);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordWithdrawal(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.WITHDRAWAL);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordTransfer(Account accountTo, Account accountFrom, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.TRANSFER);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(accountTo);
        transaction.setAccountFrom(accountFrom);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
        accountTo.getTransactionsFrom().add(transaction);
        accountFrom.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordPaymentByCard(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.CARD);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction getTransactionById(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return transaction;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsByAmount(Long account_id, BigDecimal amount, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndAmount(account_id, amount, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsInDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserInDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsBeforeDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserBeforeDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsAfterDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findTransactionsForUserAfterDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public TransactionDTO hideTransaction(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transaction.setIsHidden(true);
        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    @Override
    public TransactionDTOAdmin cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction is not found"));

        if (transaction.getStatusTransaction() != StatusTransaction.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't cancel this transaction, because it's completed");
        }

        Account sender = transaction.getAccountFrom();
        sender.setBalance(sender.getBalance().add(transaction.getAmount()));
        accountRepository.save(sender);

        transaction.setStatusTransaction(StatusTransaction.CANCELLED);
        transactionRepository.save(transaction);
        return transactionMapper.toDTOAdmin(transaction);

    }

}
