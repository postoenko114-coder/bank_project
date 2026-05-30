package com.alex.bank.mapper;

import com.alex.bank.dto.AccountDTO;
import com.alex.bank.models.account.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(source = "currencyAccount", target = "currency")
    AccountDTO toDTO(Account account);

    @Mapping(source = "currency", target = "currencyAccount")
    Account toEntity(AccountDTO accountDTO);

}
