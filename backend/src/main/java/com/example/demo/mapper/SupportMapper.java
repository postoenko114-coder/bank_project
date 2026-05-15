package com.example.demo.mapper;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.models.supportMessage.SupportMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface SupportMapper {

    SupportDTO toDTO(SupportMessage supportMessage);

    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "statusSupportMessage", constant = "NEW")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    SupportMessage toEntity(SupportDTO supportDTO);

}
