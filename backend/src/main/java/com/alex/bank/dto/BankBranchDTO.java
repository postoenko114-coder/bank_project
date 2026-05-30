package com.alex.bank.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankBranchDTO {

    private Long id;

    @NotBlank(message = "Branch name is required")
    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String bankBranchName;

    @Valid
    private LocationDTO locationDTO;

    private List<WorkingHourDTO> schedule;

    private List<BankServiceDTO> bankServices;
}
