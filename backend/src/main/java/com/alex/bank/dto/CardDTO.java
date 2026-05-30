package com.alex.bank.dto;

import com.alex.bank.models.card.StatusCard;
import com.alex.bank.models.card.TypeCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;

    private String cardNumber;

    private LocalDate expiryDate;

    private String cardHolderName;

    private TypeCard typeCard;

    private StatusCard statusCard;

}
