package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.RacketRepository;
import com.example.quanly.repository.RacketStockByDateRepository;
import com.example.quanly.repository.RentalToolRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RentalToolService {

    @Autowired
    private RentalToolRepository rentalToolRepository;
    @Autowired
    private RacketRepository racketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RacketStockByDateRepository racketStockByDateRepository;

    public List<RentalTool> getRentalByTypeDAILY() {
        return rentalToolRepository.findByType("DAILY");
    }

    public RentalTool getRentalToolById(Long id) {
        return rentalToolRepository.findById(id).orElseThrow(() -> new RuntimeException("RentalTool not found"));
    }

    @Transactional
    public RentalTool changeStatus(Long rentalToolId, RentalToolStatus status) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new RuntimeException("RentalTool not found"));
        rentalTool.setStatus(status);
        if(status == RentalToolStatus.COMPLETED) {
            completeRental(rentalToolId);
        }
        return rentalToolRepository.save(rentalTool);
    }


    public void handleSubmitRental(RentalTool rentalTool, Model model) {
        Racket racket = racketRepository.findById(Long.valueOf(rentalTool.getRacketId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vợt"));

        if (rentalTool.getType().equals("ON_SITE")) {
            handleOnSiteRental(rentalTool);
        } else if (rentalTool.getType().equals("DAILY")) {
            handleDailyRental(rentalTool);
        } else {
            throw new RuntimeException("Loại thuê không hợp lệ");
        }
        Booking booking = bookingRepository.findById(Long.valueOf(rentalTool.getBookingId())).orElse(null);
        if (booking != null) {
            model.addAttribute("bookingCode", booking.getBookingCode());
        }
        model.addAttribute("rentalTool", rentalTool);
        model.addAttribute("racket", racket);
    }

    private void handleOnSiteRental(RentalTool rentalTool) {
        String bookingCode = rentalTool.getBookingId();
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            throw new RuntimeException("Không tìm thấy booking");
        }

        rentalTool.setRentalDate(booking.getBookingDate());
        rentalTool.setBookingId(String.valueOf(booking.getId()));
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    private void handleDailyRental(RentalTool rentalTool) {
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();
        Long racketId = rentalTool.getRacketId();

        // 1. Kiểm tra tồn kho
        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new RuntimeException("Không đủ tồn kho cho ngày " + date));
            if (stock.getAvailableStock() < quantity) {
                throw new RuntimeException("Không đủ vợt vào ngày " + date);
            }
        }

        // 2. Trừ tồn kho
        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date).get();
            stock.setAvailableStock(stock.getAvailableStock() - quantity);
            stock.setReservedStock(stock.getReservedStock() + quantity);
            racketStockByDateRepository.save(stock);
        }

        // 3. Lưu đơn thuê
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }


    @Transactional
    public void completeRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuê"));

        if (!rentalTool.getStatus().equals(RentalToolStatus.PENDING)) {
            throw new RuntimeException("Đơn thuê không ở trạng thái PENDING");
        }

        Long racketId = (rentalTool.getRacketId());
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();

        // Cộng lại stock cho từng ngày
        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho ngày " + date));

            stock.setAvailableStock(stock.getAvailableStock() + quantity);
            stock.setReservedStock(stock.getReservedStock() - quantity); // Nếu bạn có dùng reserved
            racketStockByDateRepository.save(stock);
        }

        // Update trạng thái đơn thuê
        rentalTool.setStatus(RentalToolStatus.COMPLETED);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }



}
