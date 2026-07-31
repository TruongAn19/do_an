package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RentalToolMapper {

    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "equipmentName", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    RentalToolDTO toDTO(RentalTool rentalTool);

    @Mapping(target = "returnDate", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "rentalToolCode", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "onSiteStockReserved", ignore = true)
    @Mapping(target = "dailyStockReserved", ignore = true)
    RentalTool toEntity(RentalToolDTO rentalToolDTO);
}
