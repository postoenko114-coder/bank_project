package com.example.demo.mapper;

import com.example.demo.dto.AccountDTO;
import com.example.demo.models.account.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "currencyAccount", target = "currency")
    AccountDTO toDTO(Account account);

    @Mapping(source = "currency", target = "currencyAccount")
    Account toEntity(AccountDTO accountDTO);

}
