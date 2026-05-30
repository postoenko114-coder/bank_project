package com.alex.bank.repositories;

import com.alex.bank.models.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.id = :accountId AND a.user.id = :userId")
    boolean existsByIdAndUserId(@Param("accountId") Long accountId, @Param("userId") Long userId);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.accountNumber = :accountNumber AND a.user.id = :userId")
    boolean existsByAccountNumberAndUserId(@Param("accountNumber") String accountNumber, @Param("userId") Long userId);
}
