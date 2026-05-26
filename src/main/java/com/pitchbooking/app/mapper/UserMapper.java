package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.name", target = "roleName")
    UserResponseDTO toDTO(User user);
}
