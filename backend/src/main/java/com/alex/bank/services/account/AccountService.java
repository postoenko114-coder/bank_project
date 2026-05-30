package com.alex.bank.services.account;


import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.transaction.TransactionDTO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    @Transactional
    AccountDTO addAccount(Long user_id, String currencyAccount);

    @Transactional
    List<AccountDTO> getListUserAccounts(Long user_id);

    @Transactional
    AccountDTO getAccountById(Long account_id);

    @Transactional
    AccountDTO getAccountByNumber(String accountNumber);

    @Transactional
    AccountDTO blockAccount(Long account_id);

    @Transactional
    AccountDTO closeAccount(Long account_id);

    @Transactional
    AccountDTO activeAccount(Long account_id);

    @Transactional
    void removeAccount(Long account_id);

    @Transactional
    TransactionDTO transfer(Long accountFrom_id, Long accountTo_id, BigDecimal amount, String description);

    @Transactional
    TransactionDTO withdrawal(Long accountFrom_id, BigDecimal amount);

    @Transactional
    TransactionDTO deposit(Long accountTo_id, BigDecimal amount);

    @Transactional
    TransactionDTO payByCard(Long account_id, BigDecimal amount);
}
