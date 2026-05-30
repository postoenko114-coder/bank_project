package com.alex.bank.controllers.admin;

import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.dto.user.UserDTO;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.bankService.BankServiceService;
import com.alex.bank.services.reservation.ReservationService;
import com.alex.bank.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Reservations", description = "Administrative reservation management endpoints")
public class AdminReservationController {

    private final ReservationService reservationService;

    private final BankBranchService bankBranchService;

    private final BankServiceService bankServiceService;

    private final UserService userService;

    @Operation(summary = "Get all reservations as admin")
    @GetMapping
    public List<ReservationDTO> getReservations() {
        return reservationService.getAllReservations();
    }

    @Operation(summary = "Search reservations by branch, service or date as admin")
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

    @Operation(summary = "Cancel a reservation as admin")
    @PostMapping("/{reservationId}/cancelReservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO cancelReservation(@PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.cancelReservation(reservationId);
        return reservationDTO;
    }

    @Operation(summary = "Mark a reservation as completed as admin")
    @PostMapping("/{reservationId}/completeReservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO markAsCompleteReservation(@PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.completeReservation(reservationId);
        return reservationDTO;
    }

    @Operation(summary = "Create a reservation for a user as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createReservation(@RequestParam String username, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = resolveBankService(serviceName);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        UserDTO user = userService.findUserByUsername(username);
        return reservationService.addReservation(startReservation, user.getId(), bankService, bankBranch);
    }

    private BankService resolveBankService(String serviceName) {
        List<BankService> services = bankServiceService.findServiceByName(serviceName);
        if (services == null || services.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank service not found");
        }
        return services.get(0);
    }

}
