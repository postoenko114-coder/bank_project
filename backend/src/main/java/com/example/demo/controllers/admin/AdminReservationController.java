package com.example.demo.controllers.admin;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.example.demo.services.reservation.ReservationService;
import com.example.demo.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    private final BankBranchService bankBranchService;

    private final BankServiceService bankServiceService;

    private final UserService userService;

    @GetMapping
    public List<ReservationDTO> getReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/search")
    public List<ReservationDTO> getReservationsByFilters(
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) LocalDate date) {

        Long branchId = null;
        Long serviceId = null;

        if (branchName != null && !branchName.isEmpty()) {
            BankBranch branch = bankBranchService.findBranchByName(branchName);
            if (branch != null) branchId = branch.getId();
        }

        if (serviceName != null && !serviceName.isEmpty()) {
            List<BankService> services = bankServiceService.findServiceByName(serviceName);
            if (services != null && !services.isEmpty()) {
                serviceId = services.get(0).getId();
            }
        }

        return reservationService.findReservationsByServiceAndDateForBranch(branchId, serviceId, date);
    }

    @PostMapping("/{reservationId}/cancelReservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO cancelReservation(@PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.cancelReservation(reservationId);
        return reservationDTO;
    }

    @PostMapping("/{reservationId}/completeReservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO markAsCompleteReservation(@PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.completeReservation(reservationId);
        return reservationDTO;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createReservation(@RequestParam String username, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = bankServiceService.findServiceByName(serviceName).get(0);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        return reservationService.addReservation(startReservation, userService.findUserByUsername(username).getId(), bankService, bankBranch);
    }

}
