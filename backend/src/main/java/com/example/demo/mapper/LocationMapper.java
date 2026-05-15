package com.example.demo.mapper;

import com.example.demo.dto.LocationDTO;
import com.example.demo.models.branch.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDTO(Location location);

    Location toEntity(LocationDTO locationDTO);
}
