package com.alex.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankServiceDTO {

    private Long id;

    @NotBlank(message = "Service name is required")
    @Size(max = 100, message = "Service name must not exceed 100 characters")
    private String bankServiceName;

    @NotBlank(message = "Duration is required")
    private String duration;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
