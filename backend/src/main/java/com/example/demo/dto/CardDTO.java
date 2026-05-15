package com.example.demo.dto;

import com.example.demo.models.card.StatusCard;
import com.example.demo.models.card.TypeCard;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardDTO {
    private Long id;

    private String cardNumber;

    private LocalDate expiryDate;

    private String cardHolderName;

    private TypeCard typeCard;

    private StatusCard statusCard;

}
