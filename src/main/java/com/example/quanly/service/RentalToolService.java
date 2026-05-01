package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.CreateRentalRequest;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.mapper.RentalToolMapper;
import com.example.quanly.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RentalToolService {

    RentalToolRepository rentalToolRepository;
    RacketRepository racketRepository;
    BookingRepository bookingRepository;
    RacketStockByDateRepository racketStockByDateRepository;
    UserRepository userRepository;
    BookingDetailRepository bookingDetailRepository;
    RentalToolMapper rentalToolMapper;
    RentalPricingService rentalPricingService;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Page<RentalToolDTO> getRentalByTypeDAILY(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rentalDate").descending());
        return rentalToolRepository.findByType(RentalType.DAILY, pageable).map(rentalToolMapper::toDTO);
    }

    public RentalToolDTO getRentalToolById(Long id) {
        RentalTool rentalTool = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));
        return rentalToolMapper.toDTO(rentalTool);
    }

    public Page<RentalToolDTO> fetchRentalToolCode(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return rentalToolRepository.findByRentalToolCodeContaining(searchTerm, pageable).map(rentalToolMapper::toDTO);
    }

    public Page<RentalToolDTO> fetchRentalByUser(User user, Pageable pageable) {
        return rentalToolRepository.findRentalByUserId(user.getId(), pageable).map(rt -> {
            RentalToolDTO dto = rentalToolMapper.toDTO(rt);
            racketRepository.findById(rt.getRacketId()).ifPresent(r -> dto.setRacketName(r.getName()));
            if (rt.getBookingId() != null && !rt.getBookingId().isEmpty()) {
                try {
                    bookingRepository.findById(Long.parseLong(rt.getBookingId()))
                            .ifPresent(b -> dto.setBookingCode(b.getBookingCode()));
                } catch (NumberFormatException ignored) {}
            }
            return dto;
        });
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Tạo mới đơn thuê vợt từ request DTO.
     * Giá được tính hoàn toàn tại backend từ Racket entity — client không thể tự khai giá.
     *  - ON_SITE : liên kết booking, lưu ngay.
     *  - DAILY   : kiểm tra tồn kho → lưu PENDING, client gọi POST /{id}/pay tiếp theo.
     */
    @Transactional
    public RentalToolDTO handleSubmitRental(CreateRentalRequest request, User user) {
        Racket racket = racketRepository.findById(request.getRacketId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vợt id=" + request.getRacketId()));

        RentalTool rentalTool = buildRentalTool(request, racket, user);

        if (request.getType() == RentalType.ON_SITE) {
            return handleOnSiteRental(rentalTool, request.getBookingCode());
        } else {
            validateDailyRentalAvailable(rentalTool);
            return rentalToolMapper.toDTO(rentalToolRepository.save(rentalTool));
        }
    }

    /**
     * Xây dựng entity từ request. Giá được tính qua RentalPricingService.
     */
    private RentalTool buildRentalTool(CreateRentalRequest request, Racket racket, User user) {
        RentalTool rentalTool = new RentalTool();
        rentalTool.setFullName(request.getFullName());
        rentalTool.setEmail(request.getEmail());
        rentalTool.setPhone(request.getPhone());
        rentalTool.setType(request.getType());
        rentalTool.setRacketId(request.getRacketId());
        rentalTool.setProductId(racket.getProduct().getId());
        rentalTool.setQuantity(request.getQuantity());
        rentalTool.setUserId(user.getId());
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalTool.setRentalPrice(rentalPricingService.totalPrice(
                request.getType(), racket, request.getQuantity(), request.getQuantityDay()));
        rentalTool.setPrice(racket.getPrice() * request.getQuantity());

        if (request.getType() == RentalType.DAILY) {
            rentalTool.setQuantityDay(request.getQuantityDay());
            rentalTool.setRentalDate(request.getRentalDate());
        }
        return rentalTool;
    }

    private RentalToolDTO handleOnSiteRental(RentalTool rentalTool, String bookingCode) {
        User user = userRepository.findUserById(rentalTool.getUserId().longValue());
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng.");
        }

        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            throw new ResourceNotFoundException("Không tìm thấy booking với mã: " + bookingCode);
        }

        rentalTool.setRentalDate(booking.getBookingDate());
        rentalTool.setBookingId(String.valueOf(booking.getId()));
        rentalToolRepository.save(rentalTool);

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());
        double totalBookingDetailPrice = bookingDetails.stream()
                .mapToDouble(BookingDetail::getPrice)
                .sum();
        booking.setTotalPrice(totalBookingDetailPrice + rentalTool.getRentalPrice());
        booking.setRentalToolCode(rentalTool.getRentalToolCode());
        bookingRepository.save(booking);

        return rentalToolMapper.toDTO(rentalTool);
    }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    @Transactional
    public RentalToolDTO changeStatus(Long rentalToolId, RentalToolStatus status) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        if (status == RentalToolStatus.COMPLETED) {
            completeRental(rentalToolId);
        } else {
            rentalTool.setStatus(status);
        }
        return rentalToolMapper.toDTO(rentalToolRepository.save(rentalTool));
    }

    // -------------------------------------------------------------------------
    // Stock operations
    // -------------------------------------------------------------------------

    /**
     * Trừ tồn kho khi xác nhận thuê DAILY (CASH hoặc VNPay callback thành công).
     */
    @Transactional
    public void handleDailyRental(RentalTool rentalTool) {
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();
        Long racketId = rentalTool.getRacketId();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));
            stock.setAvailableStock(stock.getAvailableStock() - quantity);
            stock.setReservedStock(stock.getReservedStock() + quantity);
            racketStockByDateRepository.save(stock);
        }

        LocalDate today = LocalDate.now();
        if (!rentalDate.isAfter(today) && !rentalDate.plusDays(quantityDay).isBefore(today)) {
            for (int i = 0; i < quantityDay; i++) {
                LocalDate date = rentalDate.plusDays(i);
                if (date.equals(today)) {
                    RacketStockByDate stock = racketStockByDateRepository
                            .findByRacketIdAndDate(racketId, date)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy tồn kho cho ngày " + date));
                    if (stock.getReservedStock() >= quantity) {
                        stock.setReservedStock(stock.getReservedStock() - quantity);
                        stock.setRentalStock(stock.getRentalStock() + quantity);
                        racketStockByDateRepository.save(stock);
                    }
                }
            }
        }

        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    @Transactional
    public void completeRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (rentalTool.getStatus() != RentalToolStatus.PENDING && rentalTool.getStatus() != RentalToolStatus.PAID) {
            throw new IllegalStateException("Đơn thuê không ở trạng thái có thể hoàn thành");
        }

        Long racketId = rentalTool.getRacketId();
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));
            stock.setAvailableStock(stock.getAvailableStock() + quantity);
            stock.setReservedStock(stock.getReservedStock() - quantity);
            racketStockByDateRepository.save(stock);
        }

        rentalTool.setStatus(RentalToolStatus.COMPLETED);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    @Transactional
    @Scheduled(cron = "0 5 0 * * ?")
    public void updateRentalStockForToday() {
        LocalDate today = LocalDate.now();
        List<RentalTool> rentals = rentalToolRepository
                .findByStatusIn(List.of(RentalToolStatus.PENDING, RentalToolStatus.PAID));

        for (RentalTool rental : rentals) {
            Long racketId = rental.getRacketId();
            int quantity = rental.getQuantity();
            LocalDate rentalDate = rental.getRentalDate();
            int quantityDay = rental.getQuantityDay();

            if (!today.isBefore(rentalDate) && today.isBefore(rentalDate.plusDays(quantityDay))) {
                RacketStockByDate stock = racketStockByDateRepository
                        .findByRacketIdAndDate(racketId, today)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy tồn kho cho ngày " + today));

                if (stock.getReservedStock() >= quantity) {
                    stock.setReservedStock(stock.getReservedStock() - quantity);
                    stock.setRentalStock(stock.getRentalStock() + quantity);
                    racketStockByDateRepository.save(stock);
                }
            }
        }
    }

    private void validateDailyRentalAvailable(RentalTool rentalTool) {
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();
        Long racketId = rentalTool.getRacketId();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không đủ tồn kho cho ngày " + date));
            if (stock.getAvailableStock() < quantity) {
                throw new IllegalArgumentException("Không đủ vợt vào ngày " + date);
            }
        }
    }
}
