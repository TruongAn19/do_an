package com.example.quanly.mapper;

import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.name", target = "roleName")
    UserResponseDTO toDTO(User user);
}
