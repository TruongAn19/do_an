package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.*;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.mapper.RentalToolMapper;
import com.pitchbooking.app.repository.*;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RentalToolService {

    RentalToolRepository rentalToolRepository;
    EquipmentRepository equipmentRepository;
    BookingRepository bookingRepository;
    EquipmentStockByDateRepository equipmentStockByDateRepository;
    UserRepository userRepository;
    BookingDetailRepository bookingDetailRepository;
    RentalToolMapper rentalToolMapper;
    RentalPricingService rentalPricingService;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public Page<RentalToolDTO> getRentalByTypeDAILY(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rentalDate").descending());
        return enrichPage(rentalToolRepository.findByType(RentalType.DAILY, pageable));
    }

    public RentalToolDTO getRentalToolById(Long id) {
        RentalTool rentalTool = rentalToolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + id));
        return enrichDTO(rentalTool);
    }

    public Page<RentalToolDTO> fetchRentalToolCode(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return rentalToolRepository.findByRentalToolCodeContaining(searchTerm, pageable).map(this::enrichDTO);
    }

    public Page<RentalToolDTO> fetchRentalByUser(User user, Pageable pageable) {
        return enrichPage(rentalToolRepository.findRentalByUserId(user.getId(), pageable));
    }

    /**
     * Batched DTO enrichment: gom equipmentId + bookingId của cả trang rồi
     * load 1 lần qua {@code findAllById}, tránh N+1 query khi paginate.
     */
    private Page<RentalToolDTO> enrichPage(Page<RentalTool> page) {
        List<RentalTool> content = page.getContent();

        Set<Long> equipmentIds = content.stream()
                .map(RentalTool::getEquipmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Equipment> equipmentMap = equipmentIds.isEmpty()
                ? Map.of()
                : equipmentRepository.findAllById(equipmentIds).stream()
                        .collect(Collectors.toMap(Equipment::getId, e -> e));

        Set<Long> bookingIds = content.stream()
                .map(rt -> parseBookingId(rt.getBookingId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Booking> bookingMap = bookingIds.isEmpty()
                ? Map.of()
                : bookingRepository.findAllById(bookingIds).stream()
                        .collect(Collectors.toMap(Booking::getId, b -> b));

        return page.map(rt -> enrichDTO(rt, equipmentMap, bookingMap));
    }

    /** Single-row variant — kept for {@link #getRentalToolById} and {@link #fetchRentalToolCode}. */
    private RentalToolDTO enrichDTO(RentalTool rt) {
        Map<Long, Equipment> equipmentMap = rt.getEquipmentId() == null
                ? Map.of()
                : equipmentRepository.findById(rt.getEquipmentId())
                        .map(e -> Map.of(e.getId(), e))
                        .orElseGet(Map::of);
        Long bid = parseBookingId(rt.getBookingId());
        Map<Long, Booking> bookingMap = bid == null
                ? Map.of()
                : bookingRepository.findById(bid)
                        .map(b -> Map.of(b.getId(), b))
                        .orElseGet(Map::of);
        return enrichDTO(rt, equipmentMap, bookingMap);
    }

    private RentalToolDTO enrichDTO(RentalTool rt,
            Map<Long, Equipment> equipmentMap,
            Map<Long, Booking> bookingMap) {
        RentalToolDTO dto = rentalToolMapper.toDTO(rt);
        // status giữ nguyên enum name (PENDING, DEPOSITED, PAID, COMPLETED, CANCELLED)
        // do MapStruct set sẵn — FE so sánh với enum name và tự render label.
        if (rt.getRefundStatus() != null) {
            dto.setRefundStatus(rt.getRefundStatus().name());
        }
        if (rt.getCancelledAt() != null) {
            dto.setCancelledAt(rt.getCancelledAt().toString());
        }
        if (rt.getEquipmentId() != null) {
            Equipment eq = equipmentMap.get(rt.getEquipmentId());
            if (eq != null) {
                dto.setEquipmentName(eq.getName());
            }
        }
        Long bid = parseBookingId(rt.getBookingId());
        if (bid != null) {
            Booking b = bookingMap.get(bid);
            if (b != null) {
                dto.setBookingCode(b.getBookingCode());
                if (b.getBookingDate() != null) {
                    dto.setBookingDate(b.getBookingDate().toString());
                }
                if (b.getAvailableTime() != null) {
                    dto.setBookingTime(b.getAvailableTime().getTime().toString());
                }
            }
        }
        return dto;
    }

    private Long parseBookingId(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Tạo mới đơn thuê thiết bị từ request DTO.
     * Giá được tính hoàn toàn tại backend từ Equipment entity — client không thể tự khai giá.
     *  - ON_SITE : liên kết booking, lưu ngay.
     *  - DAILY   : kiểm tra tồn kho → lưu PENDING, client gọi POST /{id}/pay tiếp theo.
     */
    @Transactional
    public RentalToolDTO handleSubmitRental(CreateRentalRequest request, User user) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị id=" + request.getEquipmentId()));

        RentalTool rentalTool = buildRentalTool(request, equipment, user);

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
    private RentalTool buildRentalTool(CreateRentalRequest request, Equipment equipment, User user) {
        RentalTool rentalTool = new RentalTool();
        rentalTool.setFullName(request.getFullName());
        rentalTool.setEmail(request.getEmail());
        rentalTool.setPhone(request.getPhone());
        rentalTool.setType(request.getType());
        rentalTool.setEquipmentId(request.getEquipmentId());
        rentalTool.setProductId(equipment.getProduct().getId());
        rentalTool.setQuantity(request.getQuantity());
        rentalTool.setUserId(user.getId());
        rentalTool.setStatus(RentalToolStatus.PENDING);
        rentalTool.setCreateAt(LocalDateTime.now());
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalTool.setRentalPrice(rentalPricingService.totalPrice(
                request.getType(), equipment, request.getQuantity(), request.getQuantityDay()));
        rentalTool.setPrice(equipment.getPrice() * request.getQuantity());

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
        } else if (status == RentalToolStatus.CANCELLED) {
            RentalToolStatus prev = rentalTool.getStatus();
            rentalTool.setStatus(status);
            rentalTool.setCancelledAt(LocalDateTime.now());
            if (prev == RentalToolStatus.DEPOSITED || prev == RentalToolStatus.PAID) {
                rentalTool.setRefundStatus(RefundStatus.PENDING_REFUND);
                // rentalPrice = tổng tiền đã thanh toán (cho cả số lượng + số ngày).
                // price * quantity là giá tham chiếu nhân số lượng, không phải tiền cọc thực.
                rentalTool.setDepositAmount(rentalTool.getRentalPrice());
            } else {
                rentalTool.setRefundStatus(RefundStatus.NOT_APPLICABLE);
            }
        } else {
            rentalTool.setStatus(status);
        }
        return rentalToolMapper.toDTO(rentalToolRepository.save(rentalTool));
    }

    @Transactional
    public RentalToolDTO confirmRentalRefund(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        if (rentalTool.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new IllegalStateException("Đơn thuê không ở trạng thái chờ hoàn cọc");
        }
        rentalTool.setRefundStatus(RefundStatus.REFUNDED);
        rentalTool.setUpdateAt(LocalDateTime.now());
        return enrichDTO(rentalToolRepository.save(rentalTool));
    }

    public Page<RentalToolDTO> getRentalsByRefundStatus(RefundStatus refundStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cancelledAt").descending());
        Page<RentalTool> result = (refundStatus == null)
                ? rentalToolRepository.findByRefundStatusIn(
                        List.of(RefundStatus.PENDING_REFUND, RefundStatus.REFUNDED), pageable)
                : rentalToolRepository.findByRefundStatus(refundStatus, pageable);
        return enrichPage(result);
    }

    // -------------------------------------------------------------------------
    // Stock operations
    // -------------------------------------------------------------------------

    /**
     * Xác nhận thanh toán CASH cho đơn thuê: set PAID + trừ tồn kho
     * EquipmentStockByDate trong cùng một transaction. Nếu handleDailyRental
     * ném exception (vd: thiếu stock), việc set PAID cũng bị rollback — không
     * còn nguy cơ object dirty-in-memory như khi controller tự set status.
     */
    @Transactional
    public void confirmCashPayment(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findById(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        rentalTool.setStatus(RentalToolStatus.PAID);
        handleDailyRental(rentalTool);
    }

    /**
     * Trừ tồn kho khi xác nhận thuê DAILY (CASH hoặc VNPay callback thành công).
     */
    @Transactional
    public void handleDailyRental(RentalTool rentalTool) {
        if (rentalTool.getType() != RentalType.DAILY) {
            rentalTool.setUpdateAt(LocalDateTime.now());
            rentalToolRepository.save(rentalTool);
            return;
        }

        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;
        Long equipmentId = rentalTool.getEquipmentId();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            EquipmentStockByDate stock = equipmentStockByDateRepository.findByEquipmentIdAndDate(equipmentId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));
            stock.setAvailableStock(stock.getAvailableStock() - quantity);
            stock.setReservedStock(stock.getReservedStock() + quantity);
            equipmentStockByDateRepository.save(stock);
        }

        LocalDate today = LocalDate.now();
        if (!rentalDate.isAfter(today) && !rentalDate.plusDays(quantityDay).isBefore(today)) {
            for (int i = 0; i < quantityDay; i++) {
                LocalDate date = rentalDate.plusDays(i);
                if (date.equals(today)) {
                    EquipmentStockByDate stock = equipmentStockByDateRepository
                            .findByEquipmentIdAndDate(equipmentId, date)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy tồn kho cho ngày " + date));
                    if (stock.getReservedStock() >= quantity) {
                        stock.setReservedStock(stock.getReservedStock() - quantity);
                        stock.setRentalStock(stock.getRentalStock() + quantity);
                        equipmentStockByDateRepository.save(stock);
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

        if (rentalTool.getStatus() != RentalToolStatus.PENDING
                && rentalTool.getStatus() != RentalToolStatus.PAID
                && rentalTool.getStatus() != RentalToolStatus.DEPOSITED) {
            throw new IllegalStateException("Đơn thuê không ở trạng thái có thể hoàn thành");
        }

        if (rentalTool.getType() == RentalType.DAILY) {
            Long equipmentId = rentalTool.getEquipmentId();
            int quantity = rentalTool.getQuantity();
            LocalDate rentalDate = rentalTool.getRentalDate();
            int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;

            for (int i = 0; i < quantityDay; i++) {
                LocalDate date = rentalDate.plusDays(i);
                EquipmentStockByDate stock = equipmentStockByDateRepository.findByEquipmentIdAndDate(equipmentId, date)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho ngày " + date));
                stock.setAvailableStock(stock.getAvailableStock() + quantity);
                // Bucket nào hiện đang giữ qty phụ thuộc vào việc ngày này đã được
                // handleDailyRental/updateRentalStockForToday chuyển reserved→rental
                // chưa: rentalStock cho ngày đã/đang diễn ra, reservedStock cho
                // ngày còn ở tương lai. Ưu tiên rentalStock vì admin thường mark
                // COMPLETED sau khi rental đã bắt đầu.
                if (stock.getRentalStock() >= quantity) {
                    stock.setRentalStock(stock.getRentalStock() - quantity);
                } else if (stock.getReservedStock() >= quantity) {
                    stock.setReservedStock(stock.getReservedStock() - quantity);
                }
                equipmentStockByDateRepository.save(stock);
            }
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
                .findByStatusIn(List.of(RentalToolStatus.PENDING, RentalToolStatus.PAID, RentalToolStatus.DEPOSITED));

        for (RentalTool rental : rentals) {
            if (rental.getType() != RentalType.DAILY) continue;

            Long equipmentId = rental.getEquipmentId();
            int quantity = rental.getQuantity();
            LocalDate rentalDate = rental.getRentalDate();
            int quantityDay = (rental.getQuantityDay() != null) ? rental.getQuantityDay() : 1;

            if (!today.isBefore(rentalDate) && today.isBefore(rentalDate.plusDays(quantityDay))) {
                EquipmentStockByDate stock = equipmentStockByDateRepository
                        .findByEquipmentIdAndDate(equipmentId, today)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy tồn kho cho ngày " + today));

                if (stock.getReservedStock() >= quantity) {
                    stock.setReservedStock(stock.getReservedStock() - quantity);
                    stock.setRentalStock(stock.getRentalStock() + quantity);
                    equipmentStockByDateRepository.save(stock);
                }
            }
        }
    }

    private void validateDailyRentalAvailable(RentalTool rentalTool) {
        if (rentalTool.getType() != RentalType.DAILY) return;

        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;
        Long equipmentId = rentalTool.getEquipmentId();

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            EquipmentStockByDate stock = equipmentStockByDateRepository.findByEquipmentIdAndDate(equipmentId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("Không đủ tồn kho cho ngày " + date));
            if (stock.getAvailableStock() < quantity) {
                throw new IllegalArgumentException("Không đủ thiết bị vào ngày " + date);
            }
        }
    }
}
