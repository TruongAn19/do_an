package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.PitchType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.domain.dto.ProductUpsertRequest;
import com.pitchbooking.app.service.BookingStatsService;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.UploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock ProductService productService;
    @Mock UploadService uploadService;
    @Mock BookingStatsService bookingStatsService;

    @InjectMocks ProductController controller;

    @Test
    void updateProduct_preservesExistingRelationsAndQuantity() {
        User owner = new User();
        owner.setId(9L);
        AvailableTime time = new AvailableTime();
        time.setId(3L);
        Equipment equipment = new Equipment();
        equipment.setId(5L);

        Product existing = new Product();
        existing.setId(11L);
        existing.setQuantity(4);
        existing.setStatus("ACTIVE");
        existing.setUser(owner);
        existing.setAvailableTimes(Set.of(time));
        existing.setEquipments(List.of(equipment));

        ProductUpsertRequest request = new ProductUpsertRequest();
        request.setName("Sân đã sửa");
        request.setDetailDesc("Mô tả");
        request.setShortDesc("Mô tả ngắn");
        request.setAddress("Hà Nội");
        request.setAddressDetail("Quận 1");
        request.setPrice(500_000d);
        request.setSale(10L);
        request.setQuantity(99L);
        request.setPitchType(PitchType.SEVEN_ASIDE);

        ProductResponseDTO response = new ProductResponseDTO();
        response.setId(existing.getId());
        when(productService.getRawProductById(existing.getId())).thenReturn(existing);
        when(productService.handSaveProduct(existing)).thenReturn(response);

        controller.updateProduct(existing.getId(), request, null);

        assertThat(existing.getUser()).isSameAs(owner);
        assertThat(existing.getAvailableTimes()).containsExactly(time);
        assertThat(existing.getEquipments()).containsExactly(equipment);
        assertThat(existing.getQuantity()).isEqualTo(4);
        assertThat(existing.getStatus()).isEqualTo("ACTIVE");
        assertThat(existing.getName()).isEqualTo("Sân đã sửa");
        assertThat(existing.getPitchType()).isEqualTo(PitchType.SEVEN_ASIDE);
    }
}
