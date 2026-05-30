package com.alex.bank.services.transaction;


import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.mapper.TransactionMapper;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.transaction.StatusTransaction;
import com.alex.bank.models.transaction.Transaction;
import com.alex.bank.models.transaction.TypeTransaction;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.TransactionRepository;
import com.alex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListAccountTransactions(Long account_id, Pageable pageable) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(account.getUser().getId(), pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListUserTransaction(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(user_id, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListUserTransactionForAdmin(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllByUserId(user_id, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public TransactionDTO recordDeposit(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.DEPOSIT);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(account);
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        log.info("Transaction recorded transactionId={} type={} accountToId={} amount={}",
                saved.getId(), saved.getTypeTransaction(), account.getId(), amount);
        return transactionMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public TransactionDTO recordWithdrawal(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.WITHDRAWAL);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        log.info("Transaction recorded transactionId={} type={} accountFromId={} amount={}",
                saved.getId(), saved.getTypeTransaction(), account.getId(), amount);
        return transactionMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public TransactionDTO recordTransfer(Account accountTo, Account accountFrom, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.TRANSFER);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(accountTo);
        transaction.setAccountFrom(accountFrom);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription(description);
        Transaction saved = transactionRepository.save(transaction);
        accountTo.getTransactionsFrom().add(transaction);
        accountFrom.getTransactionsFrom().add(transaction);
        log.info("Transaction recorded transactionId={} type={} accountFromId={} accountToId={} amount={}",
                saved.getId(), saved.getTypeTransaction(), accountFrom.getId(), accountTo.getId(), amount);
        return transactionMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public TransactionDTO recordPaymentByCard(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.CARD);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        log.info("Transaction recorded transactionId={} type={} accountFromId={} amount={}",
                saved.getId(), saved.getTypeTransaction(), account.getId(), amount);
        return transactionMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public TransactionDetailsDTO getTransactionById(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return transactionMapper.toDetailsDTO(transaction);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListAccountTransactionsByAmount(Long account_id, BigDecimal amount, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndAmount(account_id, amount, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListAccountTransactionsInDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserInDate(account_id, date, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListAccountTransactionsBeforeDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserBeforeDate(account_id, date, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public List<TransactionDetailsDTO> getListAccountTransactionsAfterDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findTransactionsForUserAfterDate(account_id, date, pageable);
        return transactionMapper.mapListTransactionToDetailsDTO(transactions);
    }

    @Transactional
    @Override
    public TransactionDTO hideTransaction(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transaction.setIsHidden(true);
        log.info("Transaction hidden transactionId={}", transaction.getId());
        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    @Override
    public TransactionDetailsDTO cancelTransaction(Long transactionId) {
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
        log.info("Transaction cancelled transactionId={} accountFromId={} amount={}", transaction.getId(), sender.getId(), transaction.getAmount());
        return transactionMapper.toDetailsDTO(transaction);

    }

}
