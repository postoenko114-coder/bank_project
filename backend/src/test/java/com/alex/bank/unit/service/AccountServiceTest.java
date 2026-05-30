package com.alex.bank.unit.service;

import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.mapper.AccountMapper;
import com.alex.bank.mapper.AccountMapperImpl;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.account.AccountServiceImpl;
import com.alex.bank.services.notification.NotificationService;
import com.alex.bank.services.transaction.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private AccountMapper accountMapper = new AccountMapperImpl();;

    @Mock
    private TransactionService transactionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountServiceImpl accountServiceImpl;

    @Test
    void addAccount_ShouldReturnAccountDTO_WhenUserExists() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setAccounts(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountDTO result = accountServiceImpl.addAccount(1L, "USD");

        assertNotNull(result);
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void addAccount_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.addAccount(1L, "USD"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getListUserAccounts_ShouldReturnAccountDTOList_WhenUserExists() {
        Account account1 = new Account();
        account1.setCurrencyAccount(CurrencyAccount.USD);
        account1.setStatusAccount(StatusAccount.ACTIVE);
        account1.setBalance(BigDecimal.valueOf(100));

        Account account2 = new Account();
        account2.setCurrencyAccount(CurrencyAccount.EUR);
        account2.setStatusAccount(StatusAccount.ACTIVE);
        account2.setBalance(BigDecimal.valueOf(200));

        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setAccounts(List.of(account1, account2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

        List<AccountDTO> result = accountServiceImpl.getListUserAccounts(1L);

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getListUserAccounts_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.getListUserAccounts(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getAccountById_ShouldReturnAccountDTO_WhenAccountExists() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(500));
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        AccountDTO result = accountServiceImpl.getAccountById(1L);

        assertNotNull(result);
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountById_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.getAccountById(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void getAccountByNumber_ShouldReturnAccountDTO_WhenAccountExists() {
        Account fakeAccount = new Account();
        fakeAccount.setAccountNumber("123456789/0001");
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.ZERO);
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);

        when(accountRepository.findByAccountNumber("123456789/0001")).thenReturn(Optional.of(fakeAccount));

        AccountDTO result = accountServiceImpl.getAccountByNumber("123456789/0001");

        assertNotNull(result);
        verify(accountRepository, times(1)).findByAccountNumber("123456789/0001");
    }

    @Test
    void getAccountByNumber_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findByAccountNumber("000000000/0000")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.getAccountByNumber("000000000/0000"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void blockAccount_ShouldReturnAccountDTO_WhenAccountIsActive() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.ZERO);
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setUser(fakeUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        AccountDTO result = accountServiceImpl.blockAccount(1L);

        assertNotNull(result);
        assertEquals(StatusAccount.BLOCKED, fakeAccount.getStatusAccount());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void blockAccount_ShouldThrowConflict_WhenAccountAlreadyBlocked() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.BLOCKED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.blockAccount(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is already blocked", exception.getReason());
    }

    @Test
    void blockAccount_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.blockAccount(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void closeAccount_ShouldReturnAccountDTO_WhenAccountIsActive() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.ZERO);
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setUser(fakeUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        AccountDTO result = accountServiceImpl.closeAccount(1L);

        assertNotNull(result);
        assertEquals(StatusAccount.CLOSED, fakeAccount.getStatusAccount());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void closeAccount_ShouldThrowConflict_WhenAccountAlreadyClosed() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.CLOSED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.closeAccount(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is already closed", exception.getReason());
    }

    @Test
    void closeAccount_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.closeAccount(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void activeAccount_ShouldReturnAccountDTO_WhenAccountIsBlocked() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.BLOCKED);
        fakeAccount.setBalance(BigDecimal.ZERO);
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setUser(fakeUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        AccountDTO result = accountServiceImpl.activeAccount(1L);

        assertNotNull(result);
        assertEquals(StatusAccount.ACTIVE, fakeAccount.getStatusAccount());
        verify(notificationService, times(1)).notifyPersonalMessage(eq(1L), anyString());
    }

    @Test
    void activeAccount_ShouldThrowConflict_WhenAccountAlreadyActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.activeAccount(1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is already active", exception.getReason());
    }

    @Test
    void activeAccount_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.activeAccount(1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void removeAccount_ShouldCallRepositoryDelete() {
        Account fakeAccount = new Account();
        fakeAccount.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        accountServiceImpl.removeAccount(1L);

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).delete(fakeAccount);
    }

    @Test
    void deposit_ShouldReturnTransaction_WhenAccountIsActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(100));
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeAccount.setUser(fakeUser);

        TransactionDTO fakeTransaction = new TransactionDTO();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));
        when(transactionService.recordDeposit(any(Account.class), any(BigDecimal.class))).thenReturn(fakeTransaction);

        TransactionDTO result = accountServiceImpl.deposit(1L, BigDecimal.valueOf(200));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(300), fakeAccount.getBalance());
        verify(notificationService, times(1)).notifyDeposit(any(Account.class), any(BigDecimal.class));
        verify(transactionService, times(1)).recordDeposit(any(Account.class), any(BigDecimal.class));
    }

    @Test
    void deposit_ShouldThrowConflict_WhenAccountIsNotActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.BLOCKED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.deposit(1L, BigDecimal.valueOf(200)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
    }

    @Test
    void deposit_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.deposit(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    // ───── withdrawal ─────

    @Test
    void withdrawal_ShouldReturnTransaction_WhenAccountIsActiveAndHasSufficientFunds() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(500));
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeAccount.setUser(fakeUser);

        TransactionDTO fakeTransaction = new TransactionDTO();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));
        when(transactionService.recordWithdrawal(any(Account.class), any(BigDecimal.class))).thenReturn(fakeTransaction);

        TransactionDTO result = accountServiceImpl.withdrawal(1L, BigDecimal.valueOf(200));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(300), fakeAccount.getBalance());
        verify(notificationService, times(1)).notifyWithdrawal(any(Account.class), any(BigDecimal.class));
        verify(transactionService, times(1)).recordWithdrawal(any(Account.class), any(BigDecimal.class));
    }

    @Test
    void withdrawal_ShouldThrowBadRequest_WhenInsufficientFunds() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(50));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.withdrawal(1L, BigDecimal.valueOf(200)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Insufficient funds", exception.getReason());
    }

    @Test
    void withdrawal_ShouldThrowBadRequest_WhenAmountIsZeroOrNegative() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(500));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.withdrawal(1L, BigDecimal.ZERO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Amount must be positive", exception.getReason());
    }

    @Test
    void withdrawal_ShouldThrowConflict_WhenAccountIsNotActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.BLOCKED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.withdrawal(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
    }

    @Test
    void withdrawal_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.withdrawal(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    @Test
    void transfer_ShouldReturnTransaction_WhenBothAccountsAreActiveAndFundsAreSufficient() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account accountFrom = new Account();
        accountFrom.setStatusAccount(StatusAccount.ACTIVE);
        accountFrom.setBalance(BigDecimal.valueOf(1000));
        accountFrom.setCurrencyAccount(CurrencyAccount.USD);
        accountFrom.setUser(fakeUser);

        Account accountTo = new Account();
        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountTo.setBalance(BigDecimal.valueOf(200));
        accountTo.setCurrencyAccount(CurrencyAccount.USD);
        accountTo.setUser(fakeUser);

        TransactionDTO fakeTransaction = new TransactionDTO();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));
        when(transactionService.recordTransfer(any(), any(), any(), any())).thenReturn(fakeTransaction);

        TransactionDTO result = accountServiceImpl.transfer(1L, 2L, BigDecimal.valueOf(300), "Test transfer");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(700), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(500), accountTo.getBalance());
        verify(notificationService, times(1)).notifyTransfer(any(Account.class), any(Account.class), any(BigDecimal.class));
        verify(transactionService, times(1)).recordTransfer(any(), any(), any(), any());
    }

    @Test
    void transfer_ShouldThrowBadRequest_WhenInsufficientFunds() {
        Account accountFrom = new Account();
        accountFrom.setStatusAccount(StatusAccount.ACTIVE);
        accountFrom.setBalance(BigDecimal.valueOf(100));
        accountFrom.setCurrencyAccount(CurrencyAccount.USD);

        Account accountTo = new Account();
        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountTo.setBalance(BigDecimal.valueOf(200));
        accountTo.setCurrencyAccount(CurrencyAccount.USD);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.transfer(1L, 2L, BigDecimal.valueOf(500), "Test"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Insufficient funds", exception.getReason());
    }

    @Test
    void transfer_ShouldThrowBadRequest_WhenAmountIsZeroOrNegative() {
        Account accountFrom = new Account();
        accountFrom.setStatusAccount(StatusAccount.ACTIVE);
        accountFrom.setBalance(BigDecimal.valueOf(1000));
        accountFrom.setCurrencyAccount(CurrencyAccount.USD);

        Account accountTo = new Account();
        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountTo.setBalance(BigDecimal.ZERO);
        accountTo.setCurrencyAccount(CurrencyAccount.USD);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.transfer(1L, 2L, BigDecimal.ZERO, "Test"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Amount must be positive", exception.getReason());
    }

    @Test
    void transfer_ShouldThrowConflict_WhenAccountFromIsNotActive() {
        Account accountFrom = new Account();
        accountFrom.setStatusAccount(StatusAccount.BLOCKED);
        accountFrom.setCurrencyAccount(CurrencyAccount.USD);

        Account accountTo = new Account();
        accountTo.setStatusAccount(StatusAccount.ACTIVE);
        accountTo.setCurrencyAccount(CurrencyAccount.USD);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.transfer(1L, 2L, BigDecimal.valueOf(100), "Test"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
    }

    @Test
    void transfer_ShouldThrowNotFound_WhenAccountFromDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.transfer(1L, 2L, BigDecimal.valueOf(100), "Test"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

    // ───── payByCard ─────

    @Test
    void payByCard_ShouldReturnTransaction_WhenAccountIsActiveAndFundsAreSufficient() {
        User fakeUser = new User();
        fakeUser.setId(1L);

        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(500));
        fakeAccount.setCurrencyAccount(CurrencyAccount.USD);
        fakeAccount.setUser(fakeUser);

        TransactionDTO fakeTransaction = new TransactionDTO();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));
        when(transactionService.recordPaymentByCard(any(Account.class), any(BigDecimal.class))).thenReturn(fakeTransaction);

        TransactionDTO result = accountServiceImpl.payByCard(1L, BigDecimal.valueOf(100));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(400), fakeAccount.getBalance());
        verify(notificationService, times(1)).notifyPaymentByCard(any(Account.class), any(BigDecimal.class));
        verify(transactionService, times(1)).recordPaymentByCard(any(Account.class), any(BigDecimal.class));
    }

    @Test
    void payByCard_ShouldThrowBadRequest_WhenInsufficientFunds() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.ACTIVE);
        fakeAccount.setBalance(BigDecimal.valueOf(50));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.payByCard(1L, BigDecimal.valueOf(200)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Insufficient funds", exception.getReason());
    }

    @Test
    void payByCard_ShouldThrowConflict_WhenAccountIsNotActive() {
        Account fakeAccount = new Account();
        fakeAccount.setStatusAccount(StatusAccount.CLOSED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(fakeAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.payByCard(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
    }

    @Test
    void payByCard_ShouldThrowNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountServiceImpl.payByCard(1L, BigDecimal.valueOf(100)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Account not found", exception.getReason());
    }

}
