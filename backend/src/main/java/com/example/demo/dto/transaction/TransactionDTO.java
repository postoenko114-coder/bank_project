package com.example.demo.dto.transaction;

import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.TypeTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;

    private TypeTransaction typeTransaction;

    private StatusTransaction statusTransaction;

    private String accountTo;

    private String accountFrom;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    public TransactionDTO () {}

    public TransactionDTO (Long id, TypeTransaction typeTransaction, StatusTransaction statusTransaction, String accountTo, String accountFrom, BigDecimal amount, String description, LocalDateTime createdAt) {
        this.id = id;
        this.typeTransaction = typeTransaction;
        this.statusTransaction = statusTransaction;
        this.accountTo = accountTo;
        this.accountFrom = accountFrom;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

}
