package com.alex.bank.mapper;

import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.dto.transaction.TransactionDTOAdmin;
import com.alex.bank.dto.transaction.TransactionDetailsDTO;
import com.alex.bank.models.transaction.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "accountFrom.accountNumber", target = "accountFrom")
    @Mapping(source = "accountTo.accountNumber", target = "accountTo")
    TransactionDTO toDTO(Transaction transaction);

    @Mapping(source = "accountFrom.accountNumber", target = "accountFrom")
    @Mapping(source = "accountTo.accountNumber", target = "accountTo")
    TransactionDTOAdmin toDTOAdmin(Transaction transaction);

    @Mapping(source = "accountFrom.accountNumber", target = "accountFrom")
    @Mapping(source = "accountTo.accountNumber", target = "accountTo")
    TransactionDetailsDTO toDetailsDTO(Transaction transaction);

    TransactionDTO toDTO(TransactionDetailsDTO transactionDetailsDTO);

    TransactionDTOAdmin toDTOAdmin(TransactionDetailsDTO transactionDetailsDTO);

    List<TransactionDTO> mapDetailsListToDTO(List<TransactionDetailsDTO> transactionDetailsDTOs);

    List<TransactionDTOAdmin> mapDetailsListToDTOAdmin(List<TransactionDetailsDTO> transactionDetailsDTOs);

    List<TransactionDTOAdmin> mapListTransactionToDTOAdmin(List<Transaction> transactions);

    List<TransactionDetailsDTO> mapListTransactionToDetailsDTO(List<Transaction> transactions);
}
