package com.alex.bank.mapper;

import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.models.branch.BankService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BankServiceMapper {
    BankServiceDTO toDTO(BankService bankService);

    BankService toEntity(BankServiceDTO bankServiceDTO);
}
