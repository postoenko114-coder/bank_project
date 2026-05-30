package com.alex.bank.models.branch;

import com.alex.bank.models.branch.reservation.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "services")
public class BankService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankServiceName;

    private String duration;

    private String description;

    @ManyToMany
    @JoinTable(name = "bank_branch_bank_service",
            joinColumns = @JoinColumn(name = "bank_service_id"),
            inverseJoinColumns = @JoinColumn(name = "bank_branch_id"))
    private Set<BankBranch> bankBranches ;

    @OneToMany(mappedBy = "bankService",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Reservation> reservations;
}
