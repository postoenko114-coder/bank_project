package com.alex.bank.dto.transaction;

import com.alex.bank.models.transaction.StatusTransaction;
import com.alex.bank.models.transaction.TypeTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsDTO {

    private Long id;

    private TypeTransaction typeTransaction;

    private StatusTransaction statusTransaction;

    private String accountTo;

    private String accountFrom;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    private Boolean isHidden;
}
