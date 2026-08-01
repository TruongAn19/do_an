package com.pitchbooking.app.mapper;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.dto.EquipmentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquipmentResponseMapper {

    public EquipmentResponseDTO toDTO(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        Product product = equipment.getProduct();
        EquipmentResponseDTO.ProductSummary productSummary = product == null
                ? null
                : new EquipmentResponseDTO.ProductSummary(product.getId(), product.getName());

        return new EquipmentResponseDTO(
                equipment.getId(),
                equipment.getName(),
                equipment.getPrice(),
                equipment.isAvailable(),
                equipment.getFactory(),
                equipment.getImage(),
                equipment.getRentalPricePerDay(),
                equipment.getRentalPricePerPlay(),
                equipment.getBookingStockQuantity(),
                equipment.getQuantity(),
                equipment.getStatus(),
                productSummary);
    }

    public List<EquipmentResponseDTO> toDTOs(List<Equipment> equipments) {
        return equipments.stream().map(this::toDTO).toList();
    }
}
