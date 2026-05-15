package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BankBranchDTO {
    private Long id;

    private String bankBranchName;

    private LocationDTO locationDTO;

    private List<WorkingHourDTO> schedule;

    private List<BankServiceDTO> bankServices;


}
