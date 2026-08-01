package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.*;
import com.pitchbooking.app.domain.dto.CreateRentalRequest;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ForbiddenOperationException;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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
        // FE receives enum names and renders the Vietnamese labels.
        if (rt.getPaymentStatus() != null) {
            dto.setPaymentStatus(rt.getPaymentStatus().name());
        }
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
     *  - DAILY   : khóa và giữ tồn kho ngay khi tạo, sau đó lưu PENDING.
     */
    @Transactional
    public RentalToolDTO handleSubmitRental(CreateRentalRequest request, User user) {
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị id=" + request.getEquipmentId()));

        RentalTool rentalTool = buildRentalTool(request, equipment, user);

        if (request.getType() == RentalType.ON_SITE) {
            return handleOnSiteRental(rentalTool, request.getBookingCode());
        } else {
            reserveDailyStock(rentalTool);
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
        rentalTool.setPaymentStatus(RentalPaymentStatus.UNPAID);
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

        Booking booking = bookingRepository.findByBookingCodeWithLock(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy booking với mã: " + bookingCode));

        validateBookingOwnership(booking, user);
        validateBookingActiveForRental(booking);

        Equipment equipment = equipmentRepository.findByIdWithLock(rentalTool.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị id=" + rentalTool.getEquipmentId()));

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());
        validateEquipmentBelongsToBookingProduct(equipment, bookingDetails);
        reserveOnSiteStock(rentalTool, equipment);

        rentalTool.setRentalDate(booking.getBookingDate());
        rentalTool.setBookingId(String.valueOf(booking.getId()));

        double totalBookingDetailPrice = bookingDetails.stream()
                .mapToDouble(BookingDetail::getPrice)
                .sum();
        double existingRentalPrice = rentalToolRepository
                .findRentalToolsByBookingId(String.valueOf(booking.getId())).stream()
                .filter(existing -> existing.getType() == RentalType.ON_SITE)
                .filter(existing -> existing.getStatus() != RentalToolStatus.CANCELLED)
                .mapToDouble(RentalTool::getRentalPrice)
                .sum();

        rentalToolRepository.save(rentalTool);
        booking.setTotalPrice(
                totalBookingDetailPrice + existingRentalPrice + rentalTool.getRentalPrice());
        booking.setRentalToolCode(rentalTool.getRentalToolCode());
        bookingRepository.save(booking);

        return rentalToolMapper.toDTO(rentalTool);
    }

    private void validateEquipmentBelongsToBookingProduct(
            Equipment equipment,
            List<BookingDetail> bookingDetails) {
        if (equipment.getProduct() == null) {
            throw new IllegalStateException("Phụ kiện chưa được gắn với sân cha.");
        }

        Set<Long> bookingProductIds = bookingDetails.stream()
                .map(BookingDetail::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (bookingProductIds.isEmpty()) {
            throw new IllegalStateException("Booking không có thông tin sân cha.");
        }
        if (bookingProductIds.size() != 1) {
            throw new IllegalStateException("Các lượt trong booking không thuộc cùng một sân cha.");
        }
        if (!bookingProductIds.contains(equipment.getProduct().getId())) {
            throw new IllegalArgumentException("Phụ kiện không thuộc sân đã đặt.");
        }
    }

    private void reserveOnSiteStock(RentalTool rentalTool, Equipment equipment) {
        Integer quantity = rentalTool.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phụ kiện thuê phải lớn hơn 0.");
        }
        if (equipment.getBookingStockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Không đủ phụ kiện tại sân. Số lượng còn lại: "
                            + equipment.getBookingStockQuantity());
        }

        equipment.setBookingStockQuantity(equipment.getBookingStockQuantity() - quantity);
        equipmentRepository.save(equipment);
        rentalTool.setOnSiteStockReserved(true);
    }

    private void releaseOnSiteStock(RentalTool rentalTool) {
        if (rentalTool.getType() != RentalType.ON_SITE || !rentalTool.isOnSiteStockReserved()) {
            return;
        }

        Equipment equipment = equipmentRepository.findByIdWithLock(rentalTool.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị id=" + rentalTool.getEquipmentId()));
        equipment.setBookingStockQuantity(
                equipment.getBookingStockQuantity() + rentalTool.getQuantity());
        equipmentRepository.save(equipment);
        rentalTool.setOnSiteStockReserved(false);
    }

    private void validateBookingOwnership(Booking booking, User user) {
        if (booking.getUser() == null || booking.getUser().getId() != user.getId()) {
            throw new ForbiddenOperationException("Booking không thuộc người dùng đang đăng nhập.");
        }
    }

    private void validateBookingActiveForRental(Booking booking) {
        if (booking.getStatus() != BookingStatus.DA_DAT
                && booking.getStatus() != BookingStatus.DA_THANH_TOAN) {
            throw new IllegalArgumentException(
                    "Booking không ở trạng thái cho phép thuê phụ kiện: " + booking.getStatus());
        }

        if (booking.getBookingDate() == null
                || booking.getAvailableTime() == null
                || booking.getAvailableTime().getTime() == null) {
            throw new IllegalArgumentException("Booking thiếu thông tin khung giờ chơi để thuê phụ kiện tại sân.");
        }

        LocalDateTime endTime = booking.getBookingDate()
                .atTime(booking.getAvailableTime().getTime())
                .plusHours(1);
        if (LocalDateTime.now().isAfter(endTime)) {
            throw new IllegalArgumentException(
                    "Booking đã hết giờ chơi, không thể thuê thêm phụ kiện. Hết hạn lúc "
                            + endTime.toLocalDate() + " " + endTime.toLocalTime().withSecond(0).withNano(0));
        }
    }

    // -------------------------------------------------------------------------
    // Status management
    // -------------------------------------------------------------------------

    @Transactional
    public RentalToolDTO changeStatus(Long rentalToolId, RentalToolStatus status) {
        RentalTool rentalTool = rentalToolRepository.findByIdWithLock(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái thuê không được để trống.");
        }
        if (rentalTool.getStatus() == status) {
            return rentalToolMapper.toDTO(rentalTool);
        }

        if (status == RentalToolStatus.CANCELLED
                && rentalTool.getType() == RentalType.ON_SITE
                && rentalTool.getBookingId() != null
                && !rentalTool.getBookingId().isBlank()) {
            throw new BusinessConflictException(
                    "Phụ kiện đi kèm sân không thể hủy riêng. Vui lòng hủy đơn đặt sân.");
        }

        switch (status) {
            case RENTING -> startRental(rentalTool);
            case COMPLETED -> completeRentalState(rentalTool);
            case CANCELLED -> cancelRentalState(rentalTool);
            case PENDING -> throw new BusinessConflictException(
                    "Không thể chuyển đơn thuê trở lại trạng thái chờ nhận phụ kiện.");
        }

        rentalTool.setUpdateAt(LocalDateTime.now());
        return rentalToolMapper.toDTO(rentalToolRepository.save(rentalTool));
    }

    private void startRental(RentalTool rentalTool) {
        requireCurrentStatus(rentalTool, RentalToolStatus.PENDING, RentalToolStatus.RENTING);
        if (rentalTool.getType() == RentalType.DAILY) {
            moveDailyStockToRental(rentalTool);
        }
        rentalTool.setStatus(RentalToolStatus.RENTING);
    }

    private void completeRentalState(RentalTool rentalTool) {
        requireCurrentStatus(rentalTool, RentalToolStatus.RENTING, RentalToolStatus.COMPLETED);
        releaseReservedStock(rentalTool);
        if (rentalTool.getPaymentStatus() == RentalPaymentStatus.UNPAID) {
            rentalTool.setPaymentStatus(RentalPaymentStatus.PAID);
        }
        rentalTool.setStatus(RentalToolStatus.COMPLETED);
    }

    private void cancelRentalState(RentalTool rentalTool) {
        requireCurrentStatus(rentalTool, RentalToolStatus.PENDING, RentalToolStatus.CANCELLED);
        releaseReservedStock(rentalTool);
        rentalTool.setStatus(RentalToolStatus.CANCELLED);
        rentalTool.setCancelledAt(LocalDateTime.now());
        if (rentalTool.getPaymentStatus() == RentalPaymentStatus.PAID) {
            rentalTool.setRefundStatus(RefundStatus.PENDING_REFUND);
            rentalTool.setDepositAmount(rentalTool.getRentalPrice());
        } else {
            rentalTool.setRefundStatus(RefundStatus.NOT_APPLICABLE);
        }
    }

    private void requireCurrentStatus(
            RentalTool rentalTool,
            RentalToolStatus expected,
            RentalToolStatus target) {
        if (rentalTool.getStatus() != expected) {
            throw new BusinessConflictException(
                    "Không thể chuyển đơn thuê từ " + rentalTool.getStatus() + " sang " + target + ".");
        }
    }

    @Transactional
    public RentalToolDTO confirmRentalRefund(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdWithLock(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        if (rentalTool.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new BusinessConflictException("Đơn thuê không ở trạng thái chờ hoàn cọc");
        }
        rentalTool.setRefundStatus(RefundStatus.REFUNDED);
        rentalTool.setPaymentStatus(RentalPaymentStatus.REFUNDED);
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

    @Transactional
    public void confirmCashPayment(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdWithLock(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        confirmRentalPayment(rentalTool);
    }

    @Transactional
    public RentalTool confirmVnpayPayment(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdWithLock(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        if (rentalTool.getPaymentStatus() == RentalPaymentStatus.PAID) {
            return rentalTool;
        }
        confirmRentalPayment(rentalTool);
        return rentalTool;
    }

    private void confirmRentalPayment(RentalTool rentalTool) {
        if (rentalTool.getStatus() != RentalToolStatus.PENDING
                && rentalTool.getStatus() != RentalToolStatus.RENTING) {
            throw new BusinessConflictException("Đơn thuê không ở trạng thái có thể thanh toán.");
        }

        rentalTool.setPaymentStatus(RentalPaymentStatus.PAID);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

    private void reserveDailyStock(RentalTool rentalTool) {
        if (rentalTool.isDailyStockReserved()) {
            return;
        }

        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = (rentalTool.getQuantityDay() != null) ? rentalTool.getQuantityDay() : 1;
        Long equipmentId = rentalTool.getEquipmentId();
        List<EquipmentStockByDate> stocks = new ArrayList<>(quantityDay);

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            EquipmentStockByDate stock = getOrCreateDailyStockWithLock(equipmentId, date);
            if (stock.getAvailableStock() < quantity) {
                throw new IllegalArgumentException("Không đủ thiết bị vào ngày " + date);
            }
            stocks.add(stock);
        }

        for (EquipmentStockByDate stock : stocks) {
            stock.setAvailableStock(stock.getAvailableStock() - quantity);
            stock.setReservedStock(stock.getReservedStock() + quantity);
            equipmentStockByDateRepository.save(stock);
        }
        rentalTool.setDailyStockReserved(true);
    }

    private EquipmentStockByDate getOrCreateDailyStockWithLock(Long equipmentId, LocalDate date) {
        Optional<EquipmentStockByDate> existing = equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipmentId, date);
        if (existing.isPresent()) {
            return existing.get();
        }

        Equipment equipment = equipmentRepository.findByIdWithLock(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị id=" + equipmentId));

        return equipmentStockByDateRepository
                .findByEquipmentIdAndDateWithLock(equipmentId, date)
                .orElseGet(() -> {
                    EquipmentStockByDate stock = new EquipmentStockByDate();
                    stock.setEquipmentId(equipmentId);
                    stock.setDate(date);
                    stock.setTotalStock(equipment.getQuantity());
                    stock.setAvailableStock(equipment.getQuantity());
                    stock.setReservedStock(0);
                    stock.setRentalStock(0);
                    return equipmentStockByDateRepository.save(stock);
                });
    }

    private void moveDailyStockToRental(RentalTool rentalTool) {
        if (!rentalTool.isDailyStockReserved()) {
            throw new IllegalStateException("Đơn thuê DAILY chưa giữ tồn kho.");
        }

        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay() != null ? rentalTool.getQuantityDay() : 1;
        List<EquipmentStockByDate> stocks = new ArrayList<>(quantityDay);

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            EquipmentStockByDate stock = equipmentStockByDateRepository
                    .findByEquipmentIdAndDateWithLock(rentalTool.getEquipmentId(), date)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tồn kho cho ngày " + date));
            if (stock.getReservedStock() < quantity) {
                throw new IllegalStateException(
                        "Tồn kho giữ chỗ không đủ cho đơn thuê vào ngày " + date + ".");
            }
            stocks.add(stock);
        }

        for (EquipmentStockByDate stock : stocks) {
            stock.setReservedStock(stock.getReservedStock() - quantity);
            stock.setRentalStock(stock.getRentalStock() + quantity);
            equipmentStockByDateRepository.save(stock);
        }
    }

    private void releaseReservedStock(RentalTool rentalTool) {
        if (rentalTool.getType() == RentalType.DAILY) {
            releaseDailyStock(rentalTool);
        } else if (rentalTool.getType() == RentalType.ON_SITE) {
            releaseOnSiteStock(rentalTool);
        }
    }

    private void releaseDailyStock(RentalTool rentalTool) {
        if (!rentalTool.isDailyStockReserved()) {
            return;
        }

        int quantity = rentalTool.getQuantity();
        LocalDate rentalDate = rentalTool.getRentalDate();
        int quantityDay = rentalTool.getQuantityDay() != null ? rentalTool.getQuantityDay() : 1;
        List<EquipmentStockByDate> stocks = new ArrayList<>(quantityDay);
        boolean renting = rentalTool.getStatus() == RentalToolStatus.RENTING;

        for (int i = 0; i < quantityDay; i++) {
            LocalDate date = rentalDate.plusDays(i);
            EquipmentStockByDate stock = equipmentStockByDateRepository
                    .findByEquipmentIdAndDateWithLock(rentalTool.getEquipmentId(), date)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy tồn kho cho ngày " + date));
            int heldStock = renting ? stock.getRentalStock() : stock.getReservedStock();
            if (heldStock < quantity) {
                throw new IllegalStateException(
                        "Tồn kho không còn giữ đủ số lượng của đơn thuê vào ngày " + date + ".");
            }
            stocks.add(stock);
        }

        for (EquipmentStockByDate stock : stocks) {
            if (renting) {
                stock.setRentalStock(stock.getRentalStock() - quantity);
            } else {
                stock.setReservedStock(stock.getReservedStock() - quantity);
            }
            stock.setAvailableStock(stock.getAvailableStock() + quantity);
            equipmentStockByDateRepository.save(stock);
        }
        rentalTool.setDailyStockReserved(false);
    }

    @Transactional
    public void completeRental(Long rentalToolId) {
        RentalTool rentalTool = rentalToolRepository.findByIdWithLock(rentalToolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê id=" + rentalToolId));
        completeRentalState(rentalTool);
        rentalTool.setUpdateAt(LocalDateTime.now());
        rentalToolRepository.save(rentalTool);
    }

}
