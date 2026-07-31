package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.dto.CheckStockRequest;
import com.pitchbooking.app.domain.dto.EquipmentStockAvailabilityResponse;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.EquipmentRepository;
import com.pitchbooking.app.repository.EquipmentStockByDateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentStockByDateServiceTest {

    @Mock EquipmentRepository equipmentRepository;
    @Mock EquipmentStockByDateRepository equipmentStockByDateRepository;

    @InjectMocks EquipmentStockByDateService service;

    @Test
    void missingStockRow_returnsDerivedSnapshotWithoutWritingDatabase() {
        LocalDate date = LocalDate.now().plusDays(30);
        Equipment equipment = equipment(7L, 12);
        CheckStockRequest request = request(equipment.getId(), date);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(equipmentStockByDateRepository.findByEquipmentIdAndDate(equipment.getId(), date))
                .thenReturn(Optional.empty());

        EquipmentStockAvailabilityResponse response = service.getStock(request);

        assertThat(response.getEquipmentId()).isEqualTo(equipment.getId());
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getAvailableStock()).isEqualTo(12);
        assertThat(response.getReservedStock()).isZero();
        assertThat(response.getRentalStock()).isZero();
        assertThat(response.getTotalStock()).isEqualTo(12);
        verify(equipmentStockByDateRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void existingStockRow_returnsPersistedCountersWithoutWritingDatabase() {
        LocalDate date = LocalDate.now();
        Equipment equipment = equipment(7L, 12);
        EquipmentStockByDate stock = new EquipmentStockByDate();
        stock.setEquipmentId(equipment.getId());
        stock.setDate(date);
        stock.setAvailableStock(8);
        stock.setReservedStock(3);
        stock.setRentalStock(1);
        stock.setTotalStock(12);
        CheckStockRequest request = request(equipment.getId(), date);

        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));
        when(equipmentStockByDateRepository.findByEquipmentIdAndDate(equipment.getId(), date))
                .thenReturn(Optional.of(stock));

        EquipmentStockAvailabilityResponse response = service.getStock(request);

        assertThat(response.getAvailableStock()).isEqualTo(8);
        assertThat(response.getReservedStock()).isEqualTo(3);
        assertThat(response.getRentalStock()).isEqualTo(1);
        verify(equipmentStockByDateRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unknownEquipment_isRejectedWithoutCreatingStock() {
        CheckStockRequest request = request(999L, LocalDate.now());
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStock(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Không tìm thấy thiết bị id=999");

        verify(equipmentStockByDateRepository, never())
                .findByEquipmentIdAndDate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(equipmentStockByDateRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateFutureStockCapacity_preservesReservedAndRentalCounters() {
        EquipmentStockByDate stock = new EquipmentStockByDate();
        stock.setEquipmentId(7L);
        stock.setDate(LocalDate.now().plusDays(1));
        stock.setTotalStock(12);
        stock.setAvailableStock(7);
        stock.setReservedStock(3);
        stock.setRentalStock(2);

        when(equipmentStockByDateRepository.findFutureStocksWithLock(
                stock.getEquipmentId(), LocalDate.now()))
                .thenReturn(List.of(stock));

        service.updateFutureStockCapacity(stock.getEquipmentId(), 10);

        assertThat(stock.getTotalStock()).isEqualTo(10);
        assertThat(stock.getAvailableStock()).isEqualTo(5);
        assertThat(stock.getReservedStock()).isEqualTo(3);
        assertThat(stock.getRentalStock()).isEqualTo(2);
        verify(equipmentStockByDateRepository).saveAll(List.of(stock));
    }

    @Test
    void updateFutureStockCapacity_rejectsQuantityBelowUnavailableStock() {
        EquipmentStockByDate stock = new EquipmentStockByDate();
        stock.setEquipmentId(7L);
        stock.setDate(LocalDate.now().plusDays(1));
        stock.setTotalStock(12);
        stock.setAvailableStock(7);
        stock.setReservedStock(3);
        stock.setRentalStock(2);

        when(equipmentStockByDateRepository.findFutureStocksWithLock(
                stock.getEquipmentId(), LocalDate.now()))
                .thenReturn(List.of(stock));

        assertThatThrownBy(() -> service.updateFutureStockCapacity(
                stock.getEquipmentId(), 4))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("đang có 5 thiết bị");

        assertThat(stock.getTotalStock()).isEqualTo(12);
        assertThat(stock.getAvailableStock()).isEqualTo(7);
        verify(equipmentStockByDateRepository, never())
                .saveAll(org.mockito.ArgumentMatchers.any());
    }

    private Equipment equipment(Long id, int quantity) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setQuantity(quantity);
        return equipment;
    }

    private CheckStockRequest request(Long equipmentId, LocalDate date) {
        CheckStockRequest request = new CheckStockRequest();
        request.setEquipmentId(equipmentId);
        request.setDate(date);
        return request;
    }
}
