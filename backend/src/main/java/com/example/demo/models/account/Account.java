package com.example.demo.models.account;

import com.example.demo.models.card.Card;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private CurrencyAccount currencyAccount;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private StatusAccount statusAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "accountTo",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Transaction> transactionsTo;

    @OneToMany(mappedBy = "accountFrom",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Transaction> transactionsFrom;

    @OneToOne(mappedBy = "account")
    private Card card;

    private LocalDateTime createdAt;

}
