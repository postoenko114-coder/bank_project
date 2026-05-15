package com.example.demo.mapper;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.reservation.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(source = "bankService.bankServiceName", target = "serviceName")
    @Mapping(source = "bankBranch.bankBranchName" , target = "branchName")
    @Mapping(source = "user.username" , target = "username")
    ReservationDTO toDTO(Reservation reservation);

}
