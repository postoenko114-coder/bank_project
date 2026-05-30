package com.alex.bank.controllers;

import com.alex.bank.dto.BankBranchDTO;
import com.alex.bank.services.bankBranch.BankBranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Public Branches", description = "Public bank branch catalog and branch search endpoints")
public class BankBranchController {

    private final BankBranchService bankBranchService;

    @Operation(summary = "Get all public bank branches")
    @GetMapping
    public List<BankBranchDTO> getBankBranches() {
        return bankBranchService.getAllBankBranches();
    }

    @Operation(summary = "Find public bank branches that provide a service")
    @GetMapping("/filter/service")
    public List<BankBranchDTO> getBankBranchesByService(@RequestParam String serviceName) {
        return bankBranchService.getBranchesByService(serviceName);
    }

    @Operation(summary = "Find public bank branches by city or address")
    @GetMapping("/filter/location")
    public List<BankBranchDTO> getBankBranchesByLocation(@RequestParam(required = false) String city, @RequestParam(required = false) String address) {
        return bankBranchService.getBranchesByLocation(city, address);
    }

    @Operation(summary = "Find nearest public bank branches by coordinates")
    @GetMapping("/filter/nearest")
    public List<BankBranchDTO> getNearestBankBranchesByLocation(@RequestParam Double latitude, @RequestParam Double longitude) {
        return bankBranchService.getNearestBranches(latitude, longitude);
    }

    @Operation(summary = "Get a public bank branch by id")
    @GetMapping("/{bankBranchId}")
    public BankBranchDTO getBankBranch(@PathVariable Long bankBranchId) {
        return bankBranchService.getBankBranchById(bankBranchId);
    }

}
