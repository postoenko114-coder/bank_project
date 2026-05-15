package com.example.demo.dto;

import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private Long id;

    private String accountNumber;

    private CurrencyAccount currency;

    private BigDecimal balance;

    private StatusAccount statusAccount;

    private LocalDateTime createdAt;

}
