package com.alex.bank.controllers.client;

import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.security.IsOwner;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.bankService.BankServiceService;
import com.alex.bank.services.reservation.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/reservations")
@IsOwner
@RequiredArgsConstructor
@Tag(name = "Client Reservations", description = "Client branch appointment reservation endpoints")
public class ReservationController {

    private final ReservationService reservationService;

    private final BankServiceService bankServiceService;

    private final BankBranchService bankBranchService;

    @Operation(summary = "Get all reservations of the authenticated user")
    @GetMapping
    public List<ReservationDTO> getReservations(@PathVariable Long userId) {
        return reservationService.getAllReservationsOfUser(userId);
    }

    @Operation(summary = "Get a reservation of the authenticated user by id")
    @GetMapping("/{reservationId}")
    @PreAuthorize("@userSecurity.isReservationOwner(#userId, #reservationId) or hasRole('ADMIN')")
    public ReservationDTO getReservation(@PathVariable Long userId, @PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @Operation(summary = "Create a reservation for the authenticated user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createReservation(@PathVariable Long userId, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = resolveBankService(serviceName);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        return reservationService.addReservation(startReservation, userId, bankService, bankBranch);
    }

    @Operation(summary = "Cancel a reservation of the authenticated user")
    @PutMapping("/{reservationId}/cancel")
    @PreAuthorize("@userSecurity.isReservationOwner(#userId, #reservationId) or hasRole('ADMIN')")
    public ReservationDTO cancelReservation(@PathVariable Long userId, @PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.cancelReservation(reservationId);
        return reservationDTO;
    }

    private BankService resolveBankService(String serviceName) {
        List<BankService> services = bankServiceService.findServiceByName(serviceName);
        if (services == null || services.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank service not found");
        }
        return services.get(0);
    }

}
