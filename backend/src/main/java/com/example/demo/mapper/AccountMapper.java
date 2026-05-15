package com.example.demo.mapper;

import com.example.demo.dto.AccountDTO;
import com.example.demo.models.account.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AccountMapper {
    AccountDTO toDTO(Account account);

    Account toEntity(AccountDTO accountDTO);

}
