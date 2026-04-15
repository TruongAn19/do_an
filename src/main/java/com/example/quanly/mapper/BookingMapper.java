package com.example.quanly.mapper;

import com.example.quanly.domain.Booking;
import com.example.quanly.domain.BookingDetail;
import com.example.quanly.domain.dto.BookingDetailResponseDTO;
import com.example.quanly.domain.dto.BookingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface BookingMapper {

    @Mapping(source = "user", target = "user")
    BookingResponseDTO toDTO(Booking booking);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "availableTime.id", target = "availableTimeId")
    @Mapping(source = "availableTime.time", target = "availableTime")
    @Mapping(source = "subCourt.id", target = "subCourtId")
    @Mapping(source = "subCourt.name", target = "subCourtName")
    BookingDetailResponseDTO toDetailDTO(BookingDetail bookingDetail);
}
