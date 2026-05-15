package com.example.demo.models.transaction;

import com.example.demo.models.account.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeTransaction typeTransaction;

    @Enumerated(EnumType.STRING)
    private StatusTransaction statusTransaction;

    private BigDecimal amount;

    private String description;

    @ManyToOne
    @JoinColumn(name = "accountFrom_id")
    private Account accountFrom;

    @ManyToOne
    @JoinColumn(name = "accountTo_id")
    private Account accountTo;

    private LocalDateTime createdAt;

    private Boolean isHidden = false;

}
