package com.example.demo.controllers.admin;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.services.bankBranch.BankBranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/branches")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBankBranchController {

    private final BankBranchService bankBranchService ;

    @GetMapping
    public List<BankBranchDTO> getAllBankBranches() {
        return bankBranchService.getAllBankBranches();
    }

    @GetMapping("/{bankBranchId}")
    public BankBranchDTO getBankBranchById(@PathVariable Long bankBranchId) {
        return bankBranchService.getBankBranchById(bankBranchId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankBranchDTO createBankBranch(@RequestBody BankBranchDTO bankBranchDTO) {
        BankBranchDTO newBranch = bankBranchService.addBankBranch(bankBranchDTO, bankBranchDTO.getLocationDTO());
        return newBranch;
    }

    @PutMapping("/{bankBranchId}")
    public BankBranchDTO editBankBranch(@RequestBody BankBranchDTO bankBranchDTO, @PathVariable Long bankBranchId) {
        return bankBranchService.updateBankBranch(bankBranchId, bankBranchDTO);
    }

    @PutMapping("/{bankBranchId}/services")
    public BankBranchDTO addServiceToBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        return bankBranchService.addBankServiceToBranch(bankBranchId, serviceId);
    }

    @DeleteMapping("/{bankBranchId}/services")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteServiceFromBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        bankBranchService.deleteBankServiceFromBranch(bankBranchId, serviceId);
    }

    @DeleteMapping("/{bankBranchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBankBranch(@PathVariable Long bankBranchId) {
        bankBranchService.deleteBankBranch(bankBranchId);
    }

}
