package com.alex.bank.models.branch.reservation;

import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startReservation;

    private LocalDateTime endReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bankBranch_id")
    @JsonIgnore
    private BankBranch bankBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private BankService bankService;

    @Enumerated(EnumType.STRING)
    private StatusReservation status;


}
