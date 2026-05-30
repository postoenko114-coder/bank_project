package com.alex.bank.models.card;

import com.alex.bank.models.account.Account;
import com.alex.bank.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;

    private LocalDate expiryDate;

    /*
    CVV is not stored according to PCI DSS requirements.
    Used only during transaction processing.
    private String cvvCode;
    */

    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    private StatusCard statusCard;

    @Enumerated(EnumType.STRING)
    private TypeCard typeCard;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

}
