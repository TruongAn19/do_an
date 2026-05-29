package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "user.fullName", target = "ownerName")
    @Mapping(target = "depositPrice", source = "product", qualifiedByName = "computeDeposit")
    @Mapping(target = "pitchType", source = "pitchType")
    ProductResponseDTO toDTO(Product product);

    /**
     * Deposit = price × (1 − sale/100) × 0.5.
     * Sale discount applied first, then 50%.
     */
    @Named("computeDeposit")
    static double computeDeposit(Product product) {
        if (product == null) {
            return 0.0;
        }
        return product.getPrice() * (1 - product.getSale() / 100.0) * 0.5;
    }
}
