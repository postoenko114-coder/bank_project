package com.example.demo.mapper;

import com.example.demo.dto.user.*;
import com.example.demo.models.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "realUsername", target = "username")
    UserDTO toDTO(User user);

    @Mapping(source = "realUsername", target = "username")
    UserDTOAdmin toDTOAdmin(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    UserLogin toLogin(User user);

    UserDTOForClient toDTOForClient(User user);

    User toEntity(UserDTO userDTO);

    UserDTOForClient toDTOForClient(UserDTO userDTO);

}
