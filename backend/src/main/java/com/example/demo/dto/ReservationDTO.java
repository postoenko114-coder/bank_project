package com.example.demo.dto;

import com.example.demo.models.branch.reservation.StatusReservation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long id;

    private LocalDateTime startReservation;

    private String serviceName;

    private String branchName;

    private String username;

    private LocalDateTime endReservation;

    private StatusReservation statusReservation;

}
