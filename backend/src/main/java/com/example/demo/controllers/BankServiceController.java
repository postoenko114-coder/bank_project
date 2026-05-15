package com.example.demo.controllers;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.mapper.BankServiceMapper;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankService.BankServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class BankServiceController {

    private final BankServiceService bankServiceService;

    private final BankServiceMapper bankServiceMapper;

    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankService(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String name) {
        List<BankService> bankServices = bankServiceService.findServiceByName(name);
        List<BankServiceDTO> bankServiceDTOs = new ArrayList<>();
        for (BankService bankService : bankServices) {
            bankServiceDTOs.add(bankServiceMapper.toDTO(bankService));
        }
        return bankServiceDTOs;
    }

    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

}
