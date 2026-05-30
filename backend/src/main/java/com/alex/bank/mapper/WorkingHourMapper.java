package com.alex.bank.mapper;

import com.alex.bank.dto.WorkingHourDTO;
import com.alex.bank.models.branch.WorkingHour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WorkingHourMapper {


    @Mapping(source = "day", target = "dayOfWeek")
    @Mapping(source = "openTime", target = "openTime", dateFormat = "HH:mm")
    @Mapping(source = "closeTime", target = "closeTime", dateFormat = "HH:mm")
    WorkingHourDTO toDTO(WorkingHour workingHour);

}
