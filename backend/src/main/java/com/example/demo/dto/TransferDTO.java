package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferDTO {
    private String accountTo;

    private BigDecimal amount;

    private String description;

}
