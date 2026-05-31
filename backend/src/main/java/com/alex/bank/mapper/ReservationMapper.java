package com.alex.bank.mapper;

import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.models.branch.reservation.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(source = "bankService.bankServiceName", target = "serviceName")
    @Mapping(source = "bankBranch.bankBranchName" , target = "branchName")
    @Mapping(source = "user.username" , target = "username")
    @Mapping(source = "status", target = "statusReservation")
    ReservationDTO toDTO(Reservation reservation);

}
