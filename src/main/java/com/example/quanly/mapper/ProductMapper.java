package com.example.quanly.mapper;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.dto.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "user.fullName", target = "ownerName")
    ProductResponseDTO toDTO(Product product);
}
