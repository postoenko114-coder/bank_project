package com.alex.bank.controllers.admin;

import com.alex.bank.dto.BankBranchDTO;
import com.alex.bank.services.bankBranch.BankBranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/branches")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Branches", description = "Administrative bank branch management endpoints")
public class AdminBankBranchController {

    private final BankBranchService bankBranchService ;

    @Operation(summary = "Get all bank branches as admin")
    @GetMapping
    public List<BankBranchDTO> getAllBankBranches() {
        return bankBranchService.getAllBankBranches();
    }

    @Operation(summary = "Get a bank branch by id as admin")
    @GetMapping("/{bankBranchId}")
    public BankBranchDTO getBankBranchById(@PathVariable Long bankBranchId) {
        return bankBranchService.getBankBranchById(bankBranchId);
    }

    @Operation(summary = "Create a new bank branch as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankBranchDTO createBankBranch(@Valid @RequestBody BankBranchDTO bankBranchDTO) {
        BankBranchDTO newBranch = bankBranchService.addBankBranch(bankBranchDTO, bankBranchDTO.getLocationDTO());
        return newBranch;
    }

    @Operation(summary = "Update a bank branch as admin")
    @PutMapping("/{bankBranchId}")
    public BankBranchDTO editBankBranch(@Valid @RequestBody BankBranchDTO bankBranchDTO, @PathVariable Long bankBranchId) {
        return bankBranchService.updateBankBranch(bankBranchId, bankBranchDTO);
    }

    @Operation(summary = "Attach a bank service to a branch as admin")
    @PutMapping("/{bankBranchId}/services")
    public BankBranchDTO addServiceToBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        return bankBranchService.addBankServiceToBranch(bankBranchId, serviceId);
    }

    @Operation(summary = "Remove a bank service from a branch as admin")
    @DeleteMapping("/{bankBranchId}/services")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteServiceFromBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        bankBranchService.deleteBankServiceFromBranch(bankBranchId, serviceId);
    }

    @Operation(summary = "Delete a bank branch as admin")
    @DeleteMapping("/{bankBranchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBankBranch(@PathVariable Long bankBranchId) {
        bankBranchService.deleteBankBranch(bankBranchId);
    }

}
