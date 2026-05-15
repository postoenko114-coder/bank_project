package com.example.demo.mapper;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BankServiceMapper {
    BankServiceDTO toDTO(BankService bankService);

    BankService toEntity(BankServiceDTO bankServiceDTO);
}
