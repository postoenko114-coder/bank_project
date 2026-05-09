package com.example.demo.dto;

import java.math.BigDecimal;

public class TransferDTO {
    private String accountTo;

    private BigDecimal amount;

    private String description;

    public TransferDTO() {
    }

    public TransferDTO(String accountTo, BigDecimal amount, String description) {
        this.accountTo = accountTo;
        this.amount = amount;
        this.description = description;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
