package com.example.demo.controllers.admin;

import com.example.demo.dto.AvailabilityResponse;
import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBankServiceController {

    private final BankServiceService bankServiceService;

    private final BankBranchService bankBranchService;

    public AdminBankServiceController(BankServiceService bankServiceService, BankBranchService bankBranchService) {
        this.bankServiceService = bankServiceService;
        this.bankBranchService = bankBranchService;
    }

    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankServiceById(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String serviceName) {
        List<BankService> bankServices = bankServiceService.findServiceByName(serviceName);
        List<BankServiceDTO> bankServiceDTOs = new ArrayList<>();
        for (BankService bankService : bankServices) {
            bankServiceDTOs.add(bankService.toDTO());
        }
        return bankServiceDTOs;
    }

    @GetMapping("/{bankServiceId}/availability")
    public AvailabilityResponse getAvailabilityServiceOnDate(@PathVariable Long bankServiceId, @RequestParam String branchName, @RequestParam LocalDate date) {
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        if (!bankServiceService.getAvailabilityServiceByDate(bankBranch.getId(), bankServiceId, date)) {
            return new AvailabilityResponse(false, "Service not available");
        }
        return new AvailabilityResponse(true, "Service available");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankServiceDTO addBankService(@RequestBody BankServiceDTO bankServiceDTO) {
        BankServiceDTO newBankService = bankServiceService.addService(bankServiceDTO);
        return newBankService;
    }

    @PutMapping("/{bankServiceId}/update")
    public BankServiceDTO editService(@PathVariable Long bankServiceId, @RequestBody BankServiceDTO bankServiceDTO) {
        bankServiceService.updateService(bankServiceId, bankServiceDTO);
        return bankServiceDTO;
    }

    @DeleteMapping("/{bankServiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable Long bankServiceId) {
        bankServiceService.deleteService(bankServiceId);
    }

}
