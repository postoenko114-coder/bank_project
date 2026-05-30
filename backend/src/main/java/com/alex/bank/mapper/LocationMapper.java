package com.alex.bank.mapper;

import com.alex.bank.dto.LocationDTO;
import com.alex.bank.models.branch.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDTO(Location location);

    Location toEntity(LocationDTO locationDTO);
}
