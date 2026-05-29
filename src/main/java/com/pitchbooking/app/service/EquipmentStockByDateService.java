package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.EquipmentStockByDate;
import com.pitchbooking.app.domain.dto.CheckStockRequest;
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

    /**
     * Trả tồn kho của 1 thiết bị vào 1 ngày. Nếu row chưa tồn tại (cron midnight
     * miss, hoặc ngày nằm ngoài cửa sổ 7-ngày), tạo on-demand với
     * `availableStock = equipment.quantity` rồi trả về. Tránh được trường hợp
     * controller trả null làm FE rơi vào nhánh "Hết hàng".
     */
    @Transactional
    public EquipmentStockByDate getStock(CheckStockRequest request) {
        return equipmentStockByDateRepository
                .findByEquipmentIdAndDate(request.getEquipmentId(), request.getDate())
                .orElseGet(() -> createStockForDate(request.getEquipmentId(), request.getDate()));
    }

    private EquipmentStockByDate createStockForDate(Long equipmentId, java.time.LocalDate date) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị id=" + equipmentId));
        EquipmentStockByDate stock = new EquipmentStockByDate();
        stock.setEquipmentId(equipmentId);
        stock.setDate(date);
        stock.setTotalStock(equipment.getQuantity());
        stock.setAvailableStock(equipment.getQuantity());
        stock.setReservedStock(0);
        stock.setRentalStock(0);
        return equipmentStockByDateRepository.save(stock);
    }
}
