package com.example.demo.dto.transaction;

import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.TypeTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTOAdmin extends TransactionDTO {
    private Long id;

    private TypeTransaction typeTransaction;

    private StatusTransaction statusTransaction;

    private String accountTo;

    private String accountFrom;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    private Boolean isHiddenByUser;

}
