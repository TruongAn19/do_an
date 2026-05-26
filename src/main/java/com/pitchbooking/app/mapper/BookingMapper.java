package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.dto.BookingDetailResponseDTO;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface BookingMapper {

    @Mapping(source = "user", target = "user")
    @Mapping(target = "time", expression = "java(booking.getAvailableTime() != null ? booking.getAvailableTime().getTime().toString() : \"\")")
    @Mapping(target = "courtName", expression = "java(booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty() ? booking.getBookingDetails().get(0).getProduct().getName() : \"\")")
    @Mapping(target = "status", expression = "java(booking.getStatus() != null ? booking.getStatus().getLabel() : \"\")")
    BookingResponseDTO toDTO(Booking booking);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "availableTime.id", target = "availableTimeId")
    @Mapping(source = "availableTime.time", target = "availableTime")
    @Mapping(source = "subPitch.id", target = "subPitchId")
    @Mapping(source = "subPitch.name", target = "subPitchName")
    BookingDetailResponseDTO toDetailDTO(BookingDetail bookingDetail);
}
