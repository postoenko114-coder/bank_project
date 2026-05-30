package com.alex.bank.unit.service;

import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.mapper.TransactionMapper;
import com.alex.bank.mapper.TransactionMapperImpl;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.transaction.StatusTransaction;
import com.alex.bank.models.transaction.Transaction;
import com.alex.bank.models.transaction.TypeTransaction;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.TransactionRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.transaction.TransactionServiceImpl;
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

        TransactionDTO result = transactionServiceImpl.recordDeposit(fakeAccount, BigDecimal.valueOf(200));

        assertNotNull(result);
        assertEquals(TypeTransaction.DEPOSIT, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertNull(result.getAccountTo());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordWithdrawal_ShouldReturnTransaction_WithCorrectTypeAndStatus() {
        Account fakeAccount = new Account();
        fakeAccount.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO result = transactionServiceImpl.recordWithdrawal(fakeAccount, BigDecimal.valueOf(100));

        assertNotNull(result);
        assertEquals(TypeTransaction.WITHDRAWAL, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertNull(result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordTransfer_ShouldReturnTransaction_WithBothAccountsAndDescription() {
        Account accountTo = new Account();
        accountTo.setTransactionsFrom(new ArrayList<>());

        Account accountFrom = new Account();
        accountFrom.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO result = transactionServiceImpl.recordTransfer(accountTo, accountFrom, BigDecimal.valueOf(500), "Test transfer");

        assertNotNull(result);
        assertEquals(TypeTransaction.TRANSFER, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("Test transfer", result.getDescription());
        assertNull(result.getAccountTo());
        assertNull(result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void recordPaymentByCard_ShouldReturnTransaction_WithCorrectTypeAndStatus() {
        Account fakeAccount = new Account();
        fakeAccount.setTransactionsFrom(new ArrayList<>());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO result = transactionServiceImpl.recordPaymentByCard(fakeAccount, BigDecimal.valueOf(75));

        assertNotNull(result);
        assertEquals(TypeTransaction.CARD, result.getTypeTransaction());
        assertEquals(StatusTransaction.SUCCESS, result.getStatusTransaction());
        assertEquals(BigDecimal.valueOf(75), result.getAmount());
        assertNull(result.getAccountFrom());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenTransactionExists() {
        Transaction fakeTransaction = new Transaction();
        fakeTransaction.setTypeTransaction(TypeTransaction.DEPOSIT);
        fakeTransaction.setStatusTransaction(StatusTransaction.SUCCESS);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(fakeTransaction));

        TransactionDetailsDTO result = transactionServiceImpl.getTransactionById(1L);

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
    void cancelTransaction_ShouldReturnTransactionDetailsDTO_AndRefundSender_WhenTransactionIsPending() {
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

        TransactionDetailsDTO result = transactionServiceImpl.cancelTransaction(1L);

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

        List<TransactionDetailsDTO> result = transactionServiceImpl.getListUserTransaction(1L, pageable);

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

        List<TransactionDetailsDTO> result = transactionServiceImpl.getListAccountTransactions(1L, pageable);

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
