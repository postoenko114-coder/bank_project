package com.example.demo.mapper;

import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.models.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "accountFrom.accountNumber", target = "accountFrom")
    @Mapping(source = "accountTo.accountNumber", target = "accountTo")
    TransactionDTO toDTO(Transaction transaction);

    @Mapping(source = "accountFrom.accountNumber", target = "accountFrom")
    @Mapping(source = "accountTo.accountNumber", target = "accountTo")
    @Mapping(source = "isHidden" , target = "isHiddenByUser")
    TransactionDTOAdmin toDTOAdmin(Transaction transaction);


}
