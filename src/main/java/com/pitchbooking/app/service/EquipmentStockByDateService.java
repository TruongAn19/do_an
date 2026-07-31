package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.dto.CheckStockRequest;
import com.pitchbooking.app.domain.dto.EquipmentStockAvailabilityResponse;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.EquipmentRepository;
import com.pitchbooking.app.repository.EquipmentStockByDateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EquipmentStockByDateService {

    EquipmentRepository equipmentRepository;
    EquipmentStockByDateRepository equipmentStockByDateRepository;

    /**
     * Bù lại lần chạy cron 00:00 nếu app khởi động sau midnight. Cron `@Scheduled`
     * không catch-up missed executions, nên dev/prod khởi động ban ngày sẽ không
     * có row tồn kho cho hôm nay → getStock trả null → FE hiển thị "Hết hàng".
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmStockOnStartup() {
        log.info("Khởi tạo tồn kho 7 ngày tới (warmup khi app start)");
        generateStockByDate();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateStockByDate() {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(6);
        List<Equipment> allEquipments = equipmentRepository.findAll();
        for (Equipment equipment : allEquipments) {
            for (LocalDate date = today; !date.isAfter(targetDate); date = date.plusDays(1)) {
                boolean exists = equipmentStockByDateRepository.existsByEquipmentIdAndDate(equipment.getId(), date);
                if (!exists) {
                    EquipmentStockByDate stock = new EquipmentStockByDate();
                    stock.setEquipmentId(equipment.getId());
                    stock.setDate(date);
                    stock.setTotalStock(equipment.getQuantity());
                    stock.setAvailableStock(equipment.getQuantity());
                    stock.setReservedStock(0);
                    stock.setRentalStock(0);
                    equipmentStockByDateRepository.save(stock);
                }
            }
        }
    }

    @Async
    public void generateStockForEquipment(Equipment equipment) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(6);

        List<EquipmentStockByDate> stocks = new ArrayList<>();

        for (LocalDate date = today; !date.isAfter(targetDate); date = date.plusDays(1)) {
            EquipmentStockByDate stock = new EquipmentStockByDate();
            stock.setEquipmentId(equipment.getId());
            stock.setDate(date);
            stock.setTotalStock(equipment.getQuantity());
            stock.setAvailableStock(equipment.getQuantity());
            stock.setReservedStock(0);
            stock.setRentalStock(0);
            stocks.add(stock);
        }

        equipmentStockByDateRepository.saveAll(stocks);
    }

    @Transactional
    public void updateFutureStockCapacity(Long equipmentId, int newTotalStock) {
        List<EquipmentStockByDate> stocks = equipmentStockByDateRepository
                .findFutureStocksWithLock(equipmentId, LocalDate.now());

        for (EquipmentStockByDate stock : stocks) {
            int unavailableStock = stock.getReservedStock() + stock.getRentalStock();
            if (newTotalStock < unavailableStock) {
                throw new BusinessConflictException(
                        "Không thể giảm tồn kho xuống " + newTotalStock
                                + " vào ngày " + stock.getDate()
                                + " vì đang có " + unavailableStock
                                + " thiết bị được giữ hoặc đang cho thuê.");
            }
            stock.setTotalStock(newTotalStock);
            stock.setAvailableStock(newTotalStock - unavailableStock);
        }
        equipmentStockByDateRepository.saveAll(stocks);
    }

    @Transactional(readOnly = true)
    public EquipmentStockAvailabilityResponse getStock(CheckStockRequest request) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị id=" + request.getEquipmentId()));

        return equipmentStockByDateRepository
                .findByEquipmentIdAndDate(request.getEquipmentId(), request.getDate())
                .map(this::toAvailabilityResponse)
                .orElseGet(() -> EquipmentStockAvailabilityResponse.builder()
                        .equipmentId(equipment.getId())
                        .date(request.getDate())
                        .availableStock(equipment.getQuantity())
                        .reservedStock(0)
                        .rentalStock(0)
                        .totalStock(equipment.getQuantity())
                        .build());
    }

    private EquipmentStockAvailabilityResponse toAvailabilityResponse(EquipmentStockByDate stock) {
        return EquipmentStockAvailabilityResponse.builder()
                .equipmentId(stock.getEquipmentId())
                .date(stock.getDate())
                .availableStock(stock.getAvailableStock())
                .reservedStock(stock.getReservedStock())
                .rentalStock(stock.getRentalStock())
                .totalStock(stock.getTotalStock())
                .build();
    }
}
