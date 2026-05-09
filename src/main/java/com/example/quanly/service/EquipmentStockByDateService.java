package com.example.quanly.service;

import com.example.quanly.domain.Equipment;
import com.example.quanly.domain.EquipmentStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
import com.example.quanly.repository.EquipmentRepository;
import com.example.quanly.repository.EquipmentStockByDateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EquipmentStockByDateService {

    EquipmentRepository equipmentRepository;
    EquipmentStockByDateRepository equipmentStockByDateRepository;

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

    public EquipmentStockByDate getStock(CheckStockRequest request) {
        return equipmentStockByDateRepository.findByEquipmentAndDate(request.getEquipmentId(), request.getDate());
    }
}
