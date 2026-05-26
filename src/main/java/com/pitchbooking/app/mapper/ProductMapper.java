package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "user.fullName", target = "ownerName")
    ProductResponseDTO toDTO(Product product);
}
