package com.example.demo.unit.service;

import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.mapper.TransactionMapper;
import com.example.demo.mapper.TransactionMapperImpl;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.transaction.TypeTransaction;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.transaction.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionServiceImpl;

    @Spy
    private TransactionMapper transactionMapper = new TransactionMapperImpl();

    @Test
    void recordDeposit_ShouldReturnTransaction_WithCorrectTypeAndStatus() {
        Account fakeAccount = new Account();
        fakeAccount.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionServiceImpl.recordDeposit(fakeAccount, BigDecimal.valueOf(200));

        assertNotNull(result);
        assertEquals(TypeTransaction.DEPOSIT, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertEquals(fakeAccount, result.getAccountTo());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordWithdrawal_ShouldReturnTransaction_WithCorrectTypeAndStatus() {
        Account fakeAccount = new Account();
        fakeAccount.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionServiceImpl.recordWithdrawal(fakeAccount, BigDecimal.valueOf(100));

        assertNotNull(result);
        assertEquals(TypeTransaction.WITHDRAWAL, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals(fakeAccount, result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordTransfer_ShouldReturnTransaction_WithBothAccountsAndDescription() {
        Account accountTo = new Account();
        accountTo.setTransactionsFrom(new ArrayList<>());

        Account accountFrom = new Account();
        accountFrom.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionServiceImpl.recordTransfer(accountTo, accountFrom, BigDecimal.valueOf(500), "Test transfer");

        assertNotNull(result);
        assertEquals(TypeTransaction.TRANSFER, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("Test transfer", result.getDescription());
        assertEquals(accountTo, result.getAccountTo());
        assertEquals(accountFrom, result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordPaymentByCard_ShouldReturnTransaction_WithCorrectTypeAndStatus() {
        Account fakeAccount = new Account();
        fakeAccount.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionServiceImpl.recordPaymentByCard(fakeAccount, BigDecimal.valueOf(75));

        assertNotNull(result);
        assertEquals(TypeTransaction.CARD, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(75), result.getAmount());
        assertEquals(fakeAccount, result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenTransactionExists() {
        Transaction fakeTransaction = new Transaction();
        fakeTransaction.setTypeTransaction(TypeTransaction.DEPOSIT);
        fakeTransaction.setStatusTransaction(StatusTransaction.SUCCESS);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(fakeTransaction));

        Transaction result = transactionServiceImpl.getTransactionById(1L);

        assertNotNull(result);
        assertEquals(TypeTransaction.DEPOSIT, result.getTypeTransaction());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_ShouldThrowNotFound_WhenTransactionDoesNotExist() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.getTransactionById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Transaction not found", exception.getReason());
    }

    @Test
    void hideTransaction_ShouldReturnTransactionDTO_AndSetHiddenTrue_WhenTransactionExists() {
        Account accountFrom = new Account();
        accountFrom.setAccountNumber("111111111/0001");
        accountFrom.setCurrencyAccount(CurrencyAccount.USD);

        Transaction fakeTransaction = new Transaction();
        fakeTransaction.setTypeTransaction(TypeTransaction.WITHDRAWAL);
        fakeTransaction.setStatusTransaction(StatusTransaction.SUCCESS);
        fakeTransaction.setAmount(BigDecimal.valueOf(100));
        fakeTransaction.setAccountFrom(accountFrom);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(fakeTransaction));

        TransactionDTO result = transactionServiceImpl.hideTransaction(1L);

        assertNotNull(result);
        assertTrue(fakeTransaction.getIsHidden());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void hideTransaction_ShouldThrowNotFound_WhenTransactionDoesNotExist() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.hideTransaction(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Transaction not found", exception.getReason());
    }

    @Test
    void cancelTransaction_ShouldReturnTransactionDTOAdmin_AndRefundSender_WhenTransactionIsPending() {
        Account sender = new Account();
        sender.setBalance(BigDecimal.valueOf(100));
        sender.setAccountNumber("123456789/0001");
        sender.setCurrencyAccount(CurrencyAccount.USD);

        Transaction fakeTransaction = new Transaction();
        fakeTransaction.setStatusTransaction(StatusTransaction.PENDING);
        fakeTransaction.setTypeTransaction(TypeTransaction.TRANSFER);
        fakeTransaction.setAmount(BigDecimal.valueOf(200));
        fakeTransaction.setAccountFrom(sender);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(fakeTransaction));

        TransactionDTOAdmin result = transactionServiceImpl.cancelTransaction(1L);

        assertNotNull(result);
        assertEquals(StatusTransaction.CANCELLED, fakeTransaction.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(300), sender.getBalance());
        verify(accountRepository, times(1)).save(sender);
        verify(transactionRepository, times(1)).save(fakeTransaction);
    }

    @Test
    void cancelTransaction_ShouldThrowBadRequest_WhenTransactionIsNotPending() {
        Transaction fakeTransaction = new Transaction();
        fakeTransaction.setStatusTransaction(StatusTransaction.SUCCESS);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(fakeTransaction));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.cancelTransaction(1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("You can't cancel this transaction, because it's completed", exception.getReason());
    }

    @Test
    void cancelTransaction_ShouldThrowNotFound_WhenTransactionDoesNotExist() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.cancelTransaction(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Transaction is not found", exception.getReason());
    }

    @Test
    void getListUserTransaction_ShouldReturnTransactionList_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        List<Transaction> fakeList = List.of(t1, t2);

        Pageable pageable = Pageable.unpaged();

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(transactionRepository.findAllVisibleByUser(1L, pageable)).thenReturn(fakeList);

        List<Transaction> result = transactionServiceImpl.getListUserTransaction(1L, pageable);

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).findAllVisibleByUser(1L, pageable);
    }

    @Test
    void getListUserTransaction_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.getListUserTransaction(1L, Pageable.unpaged()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getListAccountTransactions_ShouldReturnTransactionList_WhenAccountExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setUser(fakeUser);

        Transaction t1 = new Transaction();
        List<Transaction> fakeList = List.of(t1);

        Pageable pageable = Pageable.unpaged();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));
        when(transactionRepository.findAllVisibleByUser(1L, pageable)).thenReturn(fakeList);

        List<Transaction> result = transactionServiceImpl.getListAccountTransactions(1L, pageable);

        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getListAccountTransactions_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionServiceImpl.getListAccountTransactions(1L, Pageable.unpaged()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

}