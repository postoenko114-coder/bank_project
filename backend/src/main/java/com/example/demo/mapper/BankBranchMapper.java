package com.example.demo.mapper;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.dto.LocationDTO;
import com.example.demo.models.branch.BankBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, BankServiceMapper.class, WorkingHourMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BankBranchMapper {

    @Mapping(source = "location", target = "locationDTO")
    BankBranchDTO toDTO(BankBranch bankBranch);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "locationDTO", target = "location")
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "bankServices", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    BankBranch toEntity(BankBranchDTO bankBranchDTO, LocationDTO locationDTO);

}
