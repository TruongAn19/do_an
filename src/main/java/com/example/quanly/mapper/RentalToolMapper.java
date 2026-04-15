package com.example.quanly.mapper;

import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.dto.RentalToolDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalToolMapper {

    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "racketName", ignore = true)
    RentalToolDTO toDTO(RentalTool rentalTool);

    @Mapping(target = "returnDate", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "rentalToolCode", ignore = true)
    @Mapping(target = "userId", ignore = true)
    RentalTool toEntity(RentalToolDTO rentalToolDTO);
}
