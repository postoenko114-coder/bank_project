package com.alex.bank.mapper;

import com.alex.bank.dto.user.*;
import com.alex.bank.dto.user.*;
import com.alex.bank.models.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "realUsername", target = "username")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "hasPassword", expression = "java(user.getPassword() != null && !user.getPassword().isBlank())")
    UserDTO toDTO(User user);

    @Mapping(source = "realUsername", target = "username")
    @Mapping(target = "password", ignore = true)
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

    UserDTOAdmin toDTOAdminFromDTO(UserDTO userDTO);

}
