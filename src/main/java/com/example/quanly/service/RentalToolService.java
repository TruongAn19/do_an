package com.example.quanly.service;

import com.example.quanly.domain.*;
import com.example.quanly.domain.dto.CreateRentalRequest;
import com.example.quanly.domain.dto.RentalToolDTO;
import com.example.quanly.exception.BusinessConflictException;
import com.example.quanly.exception.ForbiddenOperationException;
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
import java.util.Objects;

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
        return rentalToolRepository.findByType(RentalType.DAILY, pageable).map(this::enrichDTO);
    }

    public RentalToolDTO getRentalToolById(Long id) {
        RentalTool rentalTool = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));
        return enrichDTO(rentalTool);
    }

    public Page<RentalToolDTO> fetchRentalToolCode(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return rentalToolRepository.findByRentalToolCodeContaining(searchTerm, pageable).map(this::enrichDTO);
    }

    public Page<RentalToolDTO> fetchRentalByUser(User user, Pageable pageable) {
        return rentalToolRepository.findRentalByUserId(user.getId(), pageable).map(this::enrichDTO);
    }

    private RentalToolDTO enrichDTO(RentalTool rt) {
        RentalToolDTO dto = rentalToolMapper.toDTO(rt);
        
        // 1. Lấy tên vợt
        racketRepository.findById(rt.getRacketId()).ifPresent(r -> dto.setRacketName(r.getName()));
        
        // 2. Lấy tên tài khoản (Account Name)
        if (rt.getUserId() != null) {
            userRepository.findById(rt.getUserId()).ifPresent(u -> dto.setAccountName(u.getFullName()));
        }

        // 3. Lấy thông tin từ Booking nếu có
        if (rt.getBookingId() != null && !rt.getBookingId().isEmpty()) {
            try {
                bookingRepository.findById(Long.parseLong(rt.getBookingId()))
                        .ifPresent(b -> {
                            dto.setBookingCode(b.getBookingCode());
                            if (b.getAvailableTime() != null) {
                                dto.setBookingTime(b.getAvailableTime().getTime().toString());
                            } else if (b.getBookingDetails() != null && !b.getBookingDetails().isEmpty()) {
                                AvailableTime at = b.getBookingDetails().get(0).getAvailableTime();
                                if (at != null) {
                                    dto.setBookingTime(at.getTime().toString());
                                }
                            }
                        });
            } catch (NumberFormatException ignored) {}
        }
        return dto;
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

        if (!racket.isAvailable() || "DELETED".equals(racket.getStatus())
                || racket.getProduct() == null || "DELETED".equals(racket.getProduct().getStatus())) {
            throw new BusinessConflictException("Vợt hoặc sân không còn hoạt động.");
        }
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
        
        // Ensure quantityDay is at least 1 for unboxing and stock calculation
        int qtyDay = (request.getType() == RentalType.DAILY) ? request.getQuantityDay() : 1;
        rentalTool.setQuantityDay(qtyDay);

        rentalTool.setRentalPrice(rentalPricingService.totalPrice(
                request.getType(), racket, request.getQuantity(), qtyDay));
        rentalTool.setPrice(racket.getPrice() * request.getQuantity());

        if (request.getType() == RentalType.DAILY) {
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

        if (booking.getUser() == null
                || !Objects.equals(booking.getUser().getId(), rentalTool.getUserId())) {
            throw new ForbiddenOperationException(
                    "Bạn không có quyền thuê phụ kiện cho booking này.");
        }

        Racket racket = racketRepository.findByIdForUpdate(rentalTool.getRacketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy vợt id=" + rentalTool.getRacketId()));
        if (racket.getBookingStockQuantity() < rentalTool.getQuantity()) {
            throw new BusinessConflictException(
                    "Vợt " + racket.getName() + " không đủ số lượng. Còn lại: "
                            + racket.getBookingStockQuantity() + ".");
        }
        racket.setBookingStockQuantity(
                racket.getBookingStockQuantity() - rentalTool.getQuantity());
        racketRepository.save(racket);

        rentalTool.setRentalDate(booking.getBookingDate());
        rentalTool.setBookingId(String.valueOf(booking.getId()));
        rentalTool.setStatus(RentalToolStatus.PAID);

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());
        double totalBookingDetailPrice = bookingDetails.stream()
                .mapToDouble(BookingDetail::getPrice)
                .sum();
        double existingRentalPrice = rentalToolRepository
                .findRentalToolsByBookingId(String.valueOf(booking.getId())).stream()
                .filter(existing -> existing.getStatus() != RentalToolStatus.CANCELLED)
                .mapToDouble(RentalTool::getRentalPrice)
                .sum();
        booking.setTotalPrice(
                totalBookingDetailPrice + existingRentalPrice + rentalTool.getRentalPrice());
        booking.setRentalToolCode(rentalTool.getRentalToolCode());
        rentalToolRepository.save(rentalTool);
        bookingRepository.save(booking);

        return rentalToolMapper.toDTO(rentalTool);
    }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    @Transactional
    public RentalToolDTO changeStatus(Long rentalToolId, RentalToolStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái rental không được để trống.");
        }
        RentalTool rentalTool = rentalToolRepository.findByIdForUpdate(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        RentalToolStatus currentStatus = rentalTool.getStatus();
        if (currentStatus == status) {
            return rentalToolMapper.toDTO(rentalTool);
        }

        validateStatusTransition(rentalTool.getType(), currentStatus, status);
        boolean releasesStock = status == RentalToolStatus.RETURNED
                || status == RentalToolStatus.COMPLETED
                || status == RentalToolStatus.CANCELLED;
        if (releasesStock
                && currentStatus != RentalToolStatus.PENDING
                && currentStatus != RentalToolStatus.RETURNED) {
            if (rentalTool.getType() == RentalType.DAILY) {
                releaseDailyStock(rentalTool);
            } else {
                releaseOnSiteStock(rentalTool);
            }
        }
        rentalTool.setStatus(status);
        rentalTool.setUpdateAt(LocalDateTime.now());
        return rentalToolMapper.toDTO(rentalToolRepository.save(rentalTool));
    }

    private void validateStatusTransition(
            RentalType type, RentalToolStatus current, RentalToolStatus target) {
        boolean allowed = switch (current) {
            case PENDING -> target == RentalToolStatus.CANCELLED;
            case PAID -> target == RentalToolStatus.RENTING
                    || target == RentalToolStatus.RETURNED
                    || target == RentalToolStatus.COMPLETED
                    || target == RentalToolStatus.CANCELLED;
            case RENTING -> target == RentalToolStatus.RETURNED
                    || target == RentalToolStatus.COMPLETED
                    || target == RentalToolStatus.CANCELLED;
            case RETURNED -> target == RentalToolStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new BusinessConflictException(
                    "Không thể chuyển đơn thuê " + type + " từ " + current + " sang " + target + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Stock operations
    // -------------------------------------------------------------------------

    @Transactional
    public RentalTool confirmDailyRentalPayment(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdForUpdate(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn thuê id=" + rentalToolId));

        if (rentalTool.getType() != RentalType.DAILY) {
            throw new BusinessConflictException("Chỉ đơn thuê DAILY sử dụng luồng thanh toán này.");
        }

        if (rentalTool.getStatus() == RentalToolStatus.RENTING
                || rentalTool.getStatus() == RentalToolStatus.RETURNED
                || rentalTool.getStatus() == RentalToolStatus.COMPLETED) {
            return rentalTool;
        }

        if (rentalTool.getStatus() != RentalToolStatus.PENDING) {
            throw new BusinessConflictException(
                    "Đơn thuê không ở trạng thái có thể xác nhận thanh toán.");
        }

        rentalTool.setStatus(RentalToolStatus.RENTING);
        handleDailyRental(rentalTool);
        return rentalTool;
    }

    @Transactional
    public RentalTool cancelFailedRentalPayment(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdForUpdate(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn thuê id=" + rentalToolId));
        if (rentalTool.getStatus() == RentalToolStatus.PENDING) {
            rentalTool.setStatus(RentalToolStatus.CANCELLED);
            rentalTool.setUpdateAt(LocalDateTime.now());
            return rentalToolRepository.save(rentalTool);
        }
        return rentalTool;
    }

    /**
     * Trừ tồn kho khi xác nhận thuê DAILY (CASH hoặc VNPay callback thành công).
     */
    private void handleDailyRental(RentalTool rentalTool) {
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;
        Long racketId = rentalTool.getRacketId();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository
                    .findByRacketIdAndDateForUpdate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));
            if (stock.getAvailableStock() < quantity) {
                throw new BusinessConflictException(
                        "Không đủ vợt vào ngày " + date + ". Còn lại: "
                                + stock.getAvailableStock() + ".");
            }
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
                            .findByRacketIdAndDateForUpdate(racketId, date)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy tồn kho cho ngày " + date));
                    if (stock.getReservedStock() >= quantity) {
                        stock.setReservedStock(stock.getReservedStock() - quantity);
                        stock.setRentalStock(stock.getRentalStock() + quantity);
                        racketStockByDateRepository.save(stock);
                        rentalTool.setLastStockActivatedDate(today);
                    }
                }
            }
        }

        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    @Transactional
    public void completeRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdForUpdate(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (rentalTool.getType() != RentalType.DAILY) {
            throw new BusinessConflictException("Chỉ đơn thuê DAILY sử dụng luồng hoàn trả kho theo ngày.");
        }
        if (rentalTool.getStatus() != RentalToolStatus.RENTING
                && rentalTool.getStatus() != RentalToolStatus.PAID) {
            throw new IllegalStateException("Đơn thuê không ở trạng thái có thể hoàn thành");
        }

        Long racketId = rentalTool.getRacketId();
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDateForUpdate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));

            if (stock.getRentalStock() >= quantity) {
                stock.setRentalStock(stock.getRentalStock() - quantity);
            } else if (stock.getReservedStock() >= quantity) {
                stock.setReservedStock(stock.getReservedStock() - quantity);
            } else {
                throw new BusinessConflictException(
                        "Tồn kho của đơn thuê không hợp lệ vào ngày " + date + ".");
            }
            stock.setAvailableStock(stock.getAvailableStock() + quantity);
            racketStockByDateRepository.save(stock);
        }

        rentalTool.setStatus(RentalToolStatus.COMPLETED);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    private void releaseDailyStock(RentalTool rentalTool) {
        Long racketId = rentalTool.getRacketId();
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay() != null ? rentalTool.getQuantityDay() : 1;

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository
                    .findByRacketIdAndDateForUpdate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tồn kho cho ngày " + date));
            if (stock.getRentalStock() >= quantity) {
                stock.setRentalStock(stock.getRentalStock() - quantity);
            } else if (stock.getReservedStock() >= quantity) {
                stock.setReservedStock(stock.getReservedStock() - quantity);
            } else {
                throw new BusinessConflictException(
                        "Tồn kho của đơn thuê không hợp lệ vào ngày " + date + ".");
            }
            stock.setAvailableStock(stock.getAvailableStock() + quantity);
            racketStockByDateRepository.save(stock);
        }
    }

    private void releaseOnSiteStock(RentalTool rentalTool) {
        Racket racket = racketRepository.findByIdForUpdate(rentalTool.getRacketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy vợt id=" + rentalTool.getRacketId()));
        racket.setBookingStockQuantity(
                racket.getBookingStockQuantity() + rentalTool.getQuantity());
        racketRepository.save(racket);
    }

    @Transactional
    @Scheduled(cron = "0 5 0 * * ?")
    public void updateRentalStockForToday() {
        LocalDate today = LocalDate.now();
        List<RentalTool> rentals = rentalToolRepository
                .findByTypeAndStatusIn(
                        RentalType.DAILY,
                        List.of(RentalToolStatus.PAID, RentalToolStatus.RENTING));

        for (RentalTool rental : rentals) {
            Long racketId = rental.getRacketId();
            int quantity = rental.getQuantity();
            LocalDate rentalDate = rental.getRentalDate();
            int quantityDay = (rental.getQuantityDay() != null) ? rental.getQuantityDay() : 1;

            if (!today.isBefore(rentalDate) && today.isBefore(rentalDate.plusDays(quantityDay))) {
                if (today.equals(rental.getLastStockActivatedDate())) {
                    continue;
                }
                RacketStockByDate stock = racketStockByDateRepository
                        .findByRacketIdAndDateForUpdate(racketId, today)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy tồn kho cho ngày " + today));

                if (stock.getReservedStock() >= quantity) {
                    stock.setReservedStock(stock.getReservedStock() - quantity);
                    stock.setRentalStock(stock.getRentalStock() + quantity);
                    racketStockByDateRepository.save(stock);
                    rental.setStatus(RentalToolStatus.RENTING);
                    rental.setLastStockActivatedDate(today);
                    rental.setUpdateAt(LocalDateTime.now());
                    rentalToolRepository.save(rental);
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
