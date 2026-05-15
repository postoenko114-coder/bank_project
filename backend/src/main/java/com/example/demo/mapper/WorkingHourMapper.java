package com.example.demo.mapper;

import com.example.demo.dto.WorkingHourDTO;
import com.example.demo.models.branch.WorkingHour;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WorkingHourMapper {


    @Mapping(source = "day", target = "dayOfWeek")
    @Mapping(source = "openTime", target = "openTime", dateFormat = "HH:mm")
    @Mapping(source = "closeTime", target = "closeTime", dateFormat = "HH:mm")
    WorkingHourDTO toDTO(WorkingHour workingHour);

}
