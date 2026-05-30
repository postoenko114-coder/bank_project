package com.alex.bank.dto;

import com.alex.bank.models.branch.reservation.StatusReservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;

    private LocalDateTime startReservation;

    private String serviceName;

    private String branchName;

    private String username;

    private LocalDateTime endReservation;

    private StatusReservation statusReservation;

}
