package com.alex.bank.dto;

import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;

    private String accountNumber;

    private CurrencyAccount currency;

    private BigDecimal balance;

    private StatusAccount statusAccount;

    private LocalDateTime createdAt;

}
