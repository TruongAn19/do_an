package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class RentalToolService {

    @Autowired
    private RentalToolRepository rentalToolRepository;
    @Autowired
    private RacketRepository racketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RacketStockByDateRepository racketStockByDateRepository;

    @Autowired
    private UserRepository userRepository;

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
        if (status == RentalToolStatus.COMPLETED) {
            completeRental(rentalToolId);
        } else
            rentalTool.setStatus(status);

        return rentalToolRepository.save(rentalTool);
    }


    public void handleSubmitRental(RentalTool rentalTool, Model model, User user) {
        Racket racket = racketRepository.findById(Long.valueOf(rentalTool.getRacketId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vợt"));
        rentalTool.setProductId(racket.getProduct().getId());
        if (rentalTool.getType().equals("ON_SITE")) {
            handleOnSiteRental(rentalTool, user);
            Booking booking = bookingRepository.findById(Long.valueOf(rentalTool.getBookingId())).orElse(null);
            if (booking != null) {
                model.addAttribute("bookingCode", booking.getBookingCode());
            }
            model.addAttribute("rentalTool", rentalTool);

        } else if (rentalTool.getType().equals("DAILY")) {
            handleDailyRental(rentalTool, user);
        } else {
            throw new RuntimeException("Loại thuê không hợp lệ");
        }

        model.addAttribute("racket", racket);
    }

    private void handleOnSiteRental(RentalTool rentalTool, User user) {
        String bookingCode = rentalTool.getBookingId();
        user = this.userRepository.findById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            throw new RuntimeException("Không tìm thấy booking");
        }

        rentalTool.setUser(user);
        rentalTool.setRentalDate(booking.getBookingDate());
        rentalTool.setBookingId(String.valueOf(booking.getId()));
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    @Transactional
    public void completeRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuê"));

        if (rentalTool.getStatus() != RentalToolStatus.PENDING && rentalTool.getStatus() != RentalToolStatus.PAID) {
            throw new RuntimeException("Đơn thuê không ở trạng thái có thể hoàn thanh");
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


    public List<RentalTool> fetchRentalToolCode(String rentalToolCode) {
        return rentalToolRepository.findByRentalToolCodeContainingIgnoreCase(rentalToolCode);
    }

    public List<RentalTool> fetchRentalByUser(User user) {
        return rentalToolRepository.findRentalByUser(user);
    }

    @Transactional
    @Scheduled(cron = "0 5 0 * * ?") // Chạy hàng ngày lúc 00:05
    public void updateRentalStockForToday() {
        LocalDate today = LocalDate.now();

        // Lấy tất cả đơn đang ở trạng thái PENDING hoặc PAID
        List<RentalTool> rentals = rentalToolRepository.findByStatusIn(List.of(RentalToolStatus.PENDING, RentalToolStatus.PAID));

        for (RentalTool rental : rentals) {
            Long racketId = rental.getRacketId();
            int quantity = rental.getQuantity();
            LocalDate rentalDate = rental.getRentalDate();
            int quantityDay = rental.getQuantityDay();

            // Kiểm tra nếu hôm nay nằm trong khoảng thuê
            if (!today.isBefore(rentalDate) && today.isBefore(rentalDate.plusDays(quantityDay))) {
                RacketStockByDate stock = racketStockByDateRepository
                        .findByRacketIdAndDate(racketId, today)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho ngày " + today));

                if (stock.getReservedStock() >= quantity) {
                    stock.setReservedStock(stock.getReservedStock() - quantity);
                    stock.setRentalStock(stock.getRentalStock() + quantity);
                    racketStockByDateRepository.save(stock);
                }
            }
        }
    }

    private void handleDailyRental(RentalTool rentalTool, User user) {
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();
        Long racketId = rentalTool.getRacketId();

        // 1. Kiểm tra tồn kho cho từng ngày
        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new RuntimeException("Không đủ tồn kho cho ngày " + date));
            if (stock.getAvailableStock() < quantity) {
                throw new RuntimeException("Không đủ vợt vào ngày " + date);
            }
        }

        // 2. Trừ tồn kho và tăng reservedStock
        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date).get();
            stock.setAvailableStock(stock.getAvailableStock() - quantity);
            stock.setReservedStock(stock.getReservedStock() + quantity);
            racketStockByDateRepository.save(stock);
        }

        // 3. Lưu đơn thuê
        rentalTool.setUser(user);
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);

        // 4. Nếu ngày thuê bắt đầu là hôm nay, cập nhật reserved → rental luôn
        LocalDate today = LocalDate.now();
        if (!rentalDate.isAfter(today) && !rentalDate.plusDays(quantityDay).isBefore(today)) {
            for (int i = 0; i < quantityDay; i++) {
                LocalDate date = rentalDate.plusDays(i);
                if (date.equals(today)) {
                    RacketStockByDate stock = racketStockByDateRepository
                            .findByRacketIdAndDate(racketId, date)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho ngày " + date));
                    if (stock.getReservedStock() >= quantity) {
                        stock.setReservedStock(stock.getReservedStock() - quantity);
                        stock.setRentalStock(stock.getRentalStock() + quantity);
                        racketStockByDateRepository.save(stock);
                    }
                }
            }
        }
    }
}
