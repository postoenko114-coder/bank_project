package com.example.demo.controllers.client;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.security.IsOwner;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.example.demo.services.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/reservations")
@IsOwner
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    private final BankServiceService bankServiceService;

    private final BankBranchService bankBranchService;

    @GetMapping
    public List<ReservationDTO> getReservations(@PathVariable Long userId) {
        return reservationService.getAllReservationsOfUser(userId);
    }

    @GetMapping("/{reservationId}")
    public ReservationDTO getReservation(@PathVariable Long userId, @PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createReservation(@PathVariable Long userId, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = bankServiceService.findServiceByName(serviceName).get(0);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        return reservationService.addReservation(startReservation, userId, bankService, bankBranch);
    }

    @PutMapping("/{reservationId}/cancel")
    public ReservationDTO cancelReservation(@PathVariable Long userId, @PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.cancelReservation(reservationId);
        return reservationDTO;
    }

}
