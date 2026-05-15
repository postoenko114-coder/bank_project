package com.example.demo.models.branch;

import com.example.demo.models.branch.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bank_branches")
public class BankBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankBranchName;

    @Embedded
    private Location location;

    @ManyToMany(mappedBy = "bankBranches")
    private Set<BankService> bankServices = new HashSet<>();

    @OneToMany(mappedBy = "bankBranch", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reservation> reservations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "branch_schedule", joinColumns = @JoinColumn(name = "branch_id"))
    private Set<WorkingHour> schedule = new HashSet<>();

}
