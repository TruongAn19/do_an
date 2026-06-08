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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    NotificationService notificationService;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Page<RentalToolDTO> getRentalByTypeDAILY(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rentalDate").descending());
        return rentalToolRepository.findByType(RentalType.DAILY, pageable).map(rentalToolMapper::toDTO);
    }

    /** Admin page: trả về toàn bộ rentals (cả DAILY và ON_SITE), enriched với racketName + bookingCode. */
    public Page<RentalToolDTO> getAllRentals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return rentalToolRepository.findAll(pageable).map(this::toEnrichedDTO);
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
        return rentalToolRepository.findRentalByUserId(user.getId(), pageable).map(this::toEnrichedDTO);
    }

    public List<RentalToolDTO> findRentalsByBookingId(Long bookingId) {
        return rentalToolRepository.findRentalToolsByBookingId(String.valueOf(bookingId))
                .stream()
                .map(this::toEnrichedDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public RentalToolDTO findRentalDtoById(Long id) {
        RentalTool rt = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));
        return toEnrichedDTO(rt);
    }

    private RentalToolDTO toEnrichedDTO(RentalTool rt) {
        RentalToolDTO dto = rentalToolMapper.toDTO(rt);
        if (rt.getRacketId() != null) {
            racketRepository.findById(rt.getRacketId()).ifPresent(r -> dto.setRacketName(r.getName()));
        }
        if (rt.getBookingId() != null && !rt.getBookingId().isEmpty()) {
            try {
                bookingRepository.findById(Long.parseLong(rt.getBookingId()))
                        .ifPresent(b -> dto.setBookingCode(b.getBookingCode()));
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

        validateBookingActiveForRental(booking);

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
        } else if (status == RentalToolStatus.CANCELLED) {
            cancelRental(rentalToolId);
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

        if (rentalTool.getStatus() != RentalToolStatus.PENDING && rentalTool.getStatus() != RentalToolStatus.IN_USE) {
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

    /**
     * Huỷ đơn thuê vợt + hoàn tồn kho (B1).
     *
     * <p>Chỉ áp dụng rental standalone DAILY (dùng {@link RacketStockByDate}). Vợt ON_SITE thuê kèm
     * booking dùng {@code Racket.bookingStockQuantity} và đã được cascade ở
     * {@code BookingService.cancelByUser} → KHÔNG đụng kho ở đây.
     *
     * <ul>
     *   <li>Không tìm thấy → {@link ResourceNotFoundException}.</li>
     *   <li>Đã CANCELLED → trả về bình thường (idempotent), không hoàn kho lần hai.</li>
     *   <li>COMPLETED → {@link IllegalStateException} (không cho huỷ đơn đã trả).</li>
     *   <li>PENDING → chỉ đổi trạng thái (chưa trừ kho qua handleDailyRental).</li>
     *   <li>IN_USE (DAILY) → hoàn {@link RacketStockByDate} cho từng ngày, đảo đúng bucket.</li>
     * </ul>
     */
    @Transactional
    public void cancelRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));

        RentalToolStatus current = rentalTool.getStatus();

        // Idempotent: đã huỷ rồi thì không làm gì thêm, không hoàn kho lần hai.
        if (current == RentalToolStatus.CANCELLED) {
            return;
        }
        if (current == RentalToolStatus.COMPLETED) {
            throw new IllegalStateException("Đơn thuê đã hoàn thành, không thể huỷ.");
        }

        // Hoàn kho chỉ khi đơn DAILY đã thực sự trừ kho (IN_USE). PENDING chưa trừ → bỏ qua.
        // ON_SITE: kho là bookingStockQuantity, cascade ở BookingService.cancelByUser → không đụng.
        boolean wasPaid = current == RentalToolStatus.IN_USE;
        if (wasPaid && rentalTool.getType() == RentalType.DAILY) {
            restoreDailyStock(rentalTool);
        }

        // Chỉ đơn đã thanh toán (IN_USE) mới phát sinh cọc cần hoàn. PENDING chưa thu tiền → NOT_APPLICABLE.
        RefundStatus refundStatus = wasPaid ? RefundStatus.PENDING_REFUND : RefundStatus.NOT_APPLICABLE;

        rentalTool.setStatus(RentalToolStatus.CANCELLED);
        rentalTool.setRefundStatus(refundStatus);
        rentalTool.setCancelledAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);

        // Notification cho user (nuốt lỗi như BookingService.cancelByUser — không để fail nghiệp vụ chính).
        try {
            notificationService.sendToUser(rentalTool.getUserId(), NotificationType.BOOKING_CANCELLED,
                    "RENTAL_TOOL", rentalToolId,
                    "Huỷ đơn thuê vợt",
                    String.format("Đơn thuê vợt %s đã được huỷ.", rentalTool.getRentalToolCode()));
        } catch (Exception ex) {
            log.warn("Gửi notification huỷ thuê vợt thất bại cho rentalToolId={}: {}", rentalToolId, ex.getMessage());
        }

        // Báo admin/staff CHỈ khi đơn đã thanh toán bị huỷ (có cọc cần hoàn). PENDING (job tự huỷ /
        // lệch tiền) không thu tiền nên không làm phiền admin.
        if (refundStatus == RefundStatus.PENDING_REFUND) {
            try {
                String staffMsg = String.format(
                        "User vừa huỷ đơn thuê vợt %s (SĐT: %s). Cần liên hệ hoàn cọc %,.0f VNĐ.",
                        rentalTool.getRentalToolCode(),
                        rentalTool.getPhone() != null ? rentalTool.getPhone() : "(không có)",
                        rentalTool.getRentalPrice());
                notificationService.sendToAdminStaff(NotificationType.REFUND_REQUEST,
                        "RENTAL_TOOL", rentalToolId,
                        "Có đơn thuê vợt bị huỷ", staffMsg);
            } catch (Exception ex) {
                log.warn("Gửi notification cho admin khi huỷ thuê vợt thất bại id={}: {}", rentalToolId, ex.getMessage());
            }
        }
    }

    /**
     * Admin xác nhận đã hoàn cọc cho đơn thuê đã huỷ (mirror {@code BookingService.confirmRefund}).
     * Tiền được admin & user tự liên hệ ngoài hệ thống; đây chỉ đánh dấu trạng thái + báo lại user.
     */
    @Transactional
    public void confirmRefund(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));

        if (rentalTool.getStatus() != RentalToolStatus.CANCELLED) {
            throw new IllegalArgumentException("Đơn thuê chưa bị huỷ — không có gì để xác nhận hoàn cọc.");
        }
        if (rentalTool.getRefundStatus() == RefundStatus.REFUNDED) {
            return; // Idempotent
        }
        if (rentalTool.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new IllegalArgumentException(
                    "Trạng thái hoàn cọc hiện tại không cho phép xác nhận: "
                            + (rentalTool.getRefundStatus() != null ? rentalTool.getRefundStatus().getLabel() : "không xác định"));
        }

        rentalTool.setRefundStatus(RefundStatus.REFUNDED);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);

        try {
            String msg = String.format(
                    "Cọc của đơn thuê vợt %s đã được hoàn (%,.0f VNĐ). Nếu chưa nhận được, vui lòng liên hệ admin.",
                    rentalTool.getRentalToolCode(), rentalTool.getRentalPrice());
            notificationService.sendToUser(rentalTool.getUserId(), NotificationType.REFUND_DONE,
                    "RENTAL_TOOL", rentalToolId,
                    "Đã hoàn cọc thuê vợt", msg);
        } catch (Exception ex) {
            log.warn("Gửi notification REFUND_DONE thuê vợt thất bại id={}: {}", rentalToolId, ex.getMessage());
        }
    }

    /**
     * Đảo ngược phép trừ của {@link #handleDailyRental} cho từng ngày trong kỳ thuê DAILY.
     *
     * <p>handleDailyRental trừ {@code availableStock} và cộng {@code reservedStock}; sau đó ngày
     * "hôm nay" (và mỗi ngày qua cron {@link #updateRentalStockForToday}) lại chuyển {@code reserved → rentalStock}.
     * Vì vậy lúc huỷ, {@code quantity} có thể đang nằm ở reservedStock HOẶC đã chuyển sang rentalStock.
     * Phải gỡ từ đúng bucket: ưu tiên reservedStock, thiếu bao nhiêu lấy nốt từ rentalStock — tránh
     * để reservedStock âm và rentalStock treo (đây là ca completeRental hiện đang bỏ sót).
     */
    private void restoreDailyStock(RentalTool rentalTool) {
        Long racketId = rentalTool.getRacketId();
        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            RacketStockByDate stock = racketStockByDateRepository.findByRacketIdAndDate(racketId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));

            stock.setAvailableStock(stock.getAvailableStock() + quantity);

            int fromReserved = Math.min(stock.getReservedStock(), quantity);
            stock.setReservedStock(stock.getReservedStock() - fromReserved);
            int remainder = quantity - fromReserved;
            if (remainder > 0) {
                stock.setRentalStock(stock.getRentalStock() - remainder);
            }

            racketStockByDateRepository.save(stock);
        }
    }

    @Transactional
    @Scheduled(cron = "0 5 0 * * ?")
    public void updateRentalStockForToday() {
        LocalDate today = LocalDate.now();
        List<RentalTool> rentals = rentalToolRepository
                .findByStatusIn(List.of(RentalToolStatus.PENDING, RentalToolStatus.IN_USE));

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

    /**
     * Đảm bảo booking đủ điều kiện để thuê thêm vợt theo sân (ON_SITE):
     *  - Status phải là DA_DAT (đã đặt cọc) hoặc DA_THANH_TOAN (đã thanh toán),
     *    không phải CHO_THANH_TOAN hay DA_HUY.
     *  - Khung giờ chơi chưa kết thúc — ước lượng end = bookingDate + availableTime + 1h.
     *    Sau thời điểm này coi như sân đã hết hạn, không cho thuê thêm vợt nữa.
     */
    private void validateBookingActiveForRental(Booking booking) {
        BookingStatus status = booking.getStatus();
        if (status != BookingStatus.DA_DAT && status != BookingStatus.DA_THANH_TOAN) {
            throw new IllegalArgumentException(
                    "Booking không ở trạng thái cho phép thuê thêm vợt (trạng thái hiện tại: "
                            + (status != null ? status.getLabel() : "không xác định") + ").");
        }

        AvailableTime time = booking.getAvailableTime();
        if (time == null || time.getTime() == null || booking.getBookingDate() == null) {
            return;
        }
        LocalDateTime playEnd = LocalDateTime.of(booking.getBookingDate(), time.getTime()).plusHours(1);
        if (LocalDateTime.now().isAfter(playEnd)) {
            throw new IllegalArgumentException(
                    "Booking đã hết giờ chơi (" + playEnd + "), không thể thuê thêm vợt cho mã này.");
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
