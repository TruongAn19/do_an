package com.pitchbooking.app.service;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.BookingDetail;
import com.pitchbooking.app.domain.BookingStatus;
import com.pitchbooking.app.domain.BookingType;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalPaymentStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.RentalToolStatus;
import com.pitchbooking.app.domain.RentalType;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.TemporaryBooking;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.BookingEquipmentSelection;
import com.pitchbooking.app.domain.dto.CancelBookingResponse;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import com.pitchbooking.app.domain.dto.PreparedBookingResult;
import com.pitchbooking.app.domain.dto.RentalToolDTO;
import com.pitchbooking.app.config.ContactProperties;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.*;
import com.pitchbooking.app.service.pricing.PricingService;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.exception.BusinessConflictException;
import com.pitchbooking.app.exception.ForbiddenOperationException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingService {

    BookingRepository bookingRepository;
    BookingDetailRepository bookingDetailRepository;
    RentalToolRepository rentalToolRepository;
    EquipmentRepository equipmentRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubPitchRepository subPitchRepository;
    SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;
    TemporaryBookingRepository temporaryBookingRepository;
    BookingMapper bookingMapper;
    PricingService pricingService;
    PendingBookingCache pendingBookingCache;
    ContactProperties contactProperties;

    public Page<BookingResponseDTO> fetchAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toDTO);
    }

    public Page<BookingResponseDTO> fetchBookingCode(String code, Pageable pageable) {
        return bookingRepository.findByBookingCodeContainingIgnoreCase(code, pageable).map(bookingMapper::toDTO);
    }

    public Page<BookingResponseDTO> fetchBookingsByDate(LocalDate date, Pageable pageable) {
        return bookingRepository.findByBookingDetailsDate(date, pageable).map(bookingMapper::toDTO);
    }

    public Optional<BookingResponseDTO> fetchBookingById(long id) {
        return this.bookingRepository.findById(id).map(booking -> {
            BookingResponseDTO dto = bookingMapper.toDTO(booking);
            List<RentalTool> rentals = rentalToolRepository
                    .findRentalToolsByBookingId(String.valueOf(booking.getId()));
            Set<Long> equipmentIds = rentals.stream()
                    .map(RentalTool::getEquipmentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Equipment> equipmentsById = equipmentIds.isEmpty()
                    ? Map.of()
                    : equipmentRepository.findAllById(equipmentIds).stream()
                            .collect(Collectors.toMap(Equipment::getId, equipment -> equipment));

            dto.setRentalTools(rentals.stream()
                    .map(rental -> toBookingRentalDTO(rental, equipmentsById.get(rental.getEquipmentId())))
                    .toList());
            return dto;
        });
    }

    private RentalToolDTO toBookingRentalDTO(RentalTool rental, Equipment equipment) {
        RentalToolDTO dto = new RentalToolDTO();
        dto.setId(rental.getId());
        dto.setRentalToolCode(rental.getRentalToolCode());
        dto.setType(rental.getType() != null ? rental.getType().name() : null);
        dto.setEquipmentId(rental.getEquipmentId() != null ? rental.getEquipmentId().toString() : null);
        dto.setEquipmentName(equipment != null ? equipment.getName() : null);
        dto.setProductId(rental.getProductId() != null ? rental.getProductId().toString() : null);
        dto.setQuantity(rental.getQuantity() != null ? rental.getQuantity() : 0);
        dto.setRentalPrice(rental.getRentalPrice());
        dto.setStatus(rental.getStatus() != null ? rental.getStatus().name() : null);
        dto.setPaymentStatus(rental.getPaymentStatus() != null ? rental.getPaymentStatus().name() : null);
        dto.setRentalDate(rental.getRentalDate() != null ? rental.getRentalDate().toString() : null);
        dto.setRefundStatus(rental.getRefundStatus() != null ? rental.getRefundStatus().name() : null);
        return dto;
    }

    @Transactional
    public CancelBookingResponse deleteBookingById(long id) {
        Booking booking = bookingRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + id));
        if (booking.getUser() == null) {
            throw new IllegalStateException("Booking không có người sở hữu, không thể thực hiện hủy an toàn.");
        }

        // Giữ endpoint cũ tương thích với FE, nhưng chuyển thành hủy mềm để toàn bộ
        // logic hoàn kho phụ kiện và hoàn cọc được thực thi.
        return cancelByUser(id, booking.getUser().getId(), "Hủy bởi quản trị viên");
    }

    @Transactional
    public void updateBooking(long id, String status) {
        Booking currentBooking = bookingRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking ID: " + id));
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Trạng thái booking không được để trống.");
        }

        BookingStatus target = BookingStatus.fromLabel(status);
        if (currentBooking.getStatus() == target) {
            if (target == BookingStatus.DA_THANH_TOAN) {
                completeBundledEquipmentRentals(currentBooking);
            }
            return;
        }
        if (currentBooking.getStatus() != BookingStatus.DA_DAT
                || target != BookingStatus.DA_THANH_TOAN) {
            throw new BusinessConflictException(
                    "Không thể chuyển booking từ " + currentBooking.getStatus() + " sang " + target
                            + ". Hủy booking phải đi qua luồng hủy để hoàn kho và xử lý hoàn cọc.");
        }

        currentBooking.setStatus(target);
        completeBundledEquipmentRentals(currentBooking);
        bookingRepository.save(currentBooking);
    }

    private void completeBundledEquipmentRentals(Booking booking) {
        List<RentalTool> bundled = rentalToolRepository
                .findRentalToolsByBookingId(String.valueOf(booking.getId()));
        LocalDateTime now = LocalDateTime.now();

        for (RentalTool rental : bundled) {
            if (rental.getType() != RentalType.ON_SITE
                    || rental.getStatus() == RentalToolStatus.CANCELLED) {
                continue;
            }

            if (rental.isOnSiteStockReserved()) {
                Equipment equipment = equipmentRepository.findByIdWithLock(rental.getEquipmentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy phụ kiện ID: " + rental.getEquipmentId()));
                equipment.setBookingStockQuantity(
                        equipment.getBookingStockQuantity() + rental.getQuantity());
                equipmentRepository.save(equipment);
                rental.setOnSiteStockReserved(false);
            }

            rental.setStatus(RentalToolStatus.COMPLETED);
            rental.setPaymentStatus(RentalPaymentStatus.PAID);
            rental.setUpdateAt(now);
            rentalToolRepository.save(rental);
        }
    }

    public List<RentalTool> getRentalToolsByBookingId(long id) {
        return rentalToolRepository.findRentalToolsByBookingId(String.valueOf(id));
    }

    public List<Equipment> getAvailableEquipmentsForBooking(String bookingCode, long userId) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode);
        if (booking == null) {
            throw new ResourceNotFoundException("Không tìm thấy booking với mã: " + bookingCode);
        }
        if (booking.getUser() == null || booking.getUser().getId() != userId) {
            throw new ForbiddenOperationException("Booking không thuộc người dùng đang đăng nhập.");
        }

        Set<Long> productIds = bookingDetailRepository.findByBookingId(booking.getId()).stream()
                .map(BookingDetail::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            throw new IllegalStateException("Booking không có thông tin sân cha.");
        }
        if (productIds.size() != 1) {
            throw new IllegalStateException("Các lượt trong booking không thuộc cùng một sân cha.");
        }

        return equipmentRepository.findByProductAndAvailableTrue(productIds.iterator().next());
    }

    public List<BookingResponseDTO> fetchBookingByUser(User user) {
        return this.bookingRepository.findByUser(user).stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validates and pre-computes booking data, caches it in memory, and extends the
     * TemporaryBooking hold to cover the VNPay payment window. Nothing is written to DB.
     */
    @Transactional
    public PreparedBookingResult preparePendingBooking(User user,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, long timeId, long subPitchId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate,
            List<Integer> daysOfWeek, Integer durationMonths) {
        return preparePendingBooking(user, receiverName, receiverAddress, receiverPhone,
                productId, timeId, subPitchId, bookingDate, bookingType, recurringEndDate,
                daysOfWeek, durationMonths, List.of());
    }

    @Transactional
    public PreparedBookingResult preparePendingBooking(User user,
            String receiverName, String receiverAddress, String receiverPhone,
            long productId, long timeId, long subPitchId, LocalDate bookingDate,
            String bookingType, LocalDate recurringEndDate,
            List<Integer> daysOfWeek, Integer durationMonths,
            List<BookingEquipmentSelection> selectedEquipments) {

        // 1. Kiểm tra người dùng
        user = userRepository.findUserById(user.getId());
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        BookingType type = (bookingType != null) ? BookingType.valueOf(bookingType) : BookingType.ONE_TIME;

        // 2. Lấy thông tin sân, khung giờ
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));
        SubPitch subPitch = subPitchRepository.findByIdWithLock(subPitchId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ ID: " + subPitchId));
        AvailableTime time = timeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ ID: " + timeId));

        validateBookingSelection(product, subPitch, time);
        List<PendingBookingData.EquipmentSelectionData> equipmentSelections =
                prepareEquipmentSelections(product, selectedEquipments);
        double equipmentRentalPrice = equipmentSelections.stream()
                .mapToDouble(PendingBookingData.EquipmentSelectionData::getTotalPrice)
                .sum();

        // 3. Tính toán danh sách ngày + giá (shared với /estimate)
        com.pitchbooking.app.domain.dto.BookingPriceBreakdown breakdown = pricingService
                .calculateBookingPriceBreakdown(user, product, time, type, bookingDate,
                        recurringEndDate, daysOfWeek, durationMonths);

        LocalDate finalEndDate = recurringEndDate;
        if (type == BookingType.WEEKLY_RECURRING && durationMonths != null && durationMonths > 0) {
            finalEndDate = bookingDate.plusMonths(durationMonths);
        }

        List<LocalDate> datesToBook = breakdown.getSlots().stream()
                .map(PendingBookingData.SlotData::getDate)
                .collect(Collectors.toList());

        // 4. Kiểm tra ngày đặt có hợp lệ (Tối đa 100 ngày)
        LocalDate today = LocalDate.now();
        LocalDate maxFutureDate = today.plusDays(100); // Mở rộng một chút cho gói 3 tháng
        for (LocalDate date : datesToBook) {
            if (date.isBefore(today)) {
                throw new IllegalArgumentException("Không thể đặt sân cho ngày trong quá khứ (" + date + ").");
            }
            if (date.isAfter(maxFutureDate)) {
                throw new IllegalArgumentException("Chỉ có thể đặt sân trong phạm vi 100 ngày tới.");
            }
        }

        // 5. Kiểm tra va chạm (Collision Check) cho TẤT CẢ các ngày
        List<String> conflictedDates = new ArrayList<>();
        for (LocalDate date : datesToBook) {
            Optional<BookingDetail> existingBooking = bookingDetailRepository
                    .findBySubPitchAndAvailableTimeAndDate(subPitch, time, date);
            if (existingBooking.isPresent()) {
                conflictedDates.add(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        if (!conflictedDates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sân này đã bị trùng lịch vào các ngày: " + String.join(", ", conflictedDates));
        }

        // 6. Giữ toàn bộ các ngày trong gói đến hết khung thanh toán VNPay.
        List<TemporaryBooking> holds = reservePendingBookingSlots(
                subPitch, time, bookingDate, datesToBook, user.getId());
        List<Long> holdIds = holds.stream()
                .map(TemporaryBooking::getId)
                .collect(Collectors.toList());

        // 7. Lưu vào cache (chỉ ID + snapshot field cần thiết, không phải entity refs)
        PendingBookingData data = new PendingBookingData(
                holds.get(0).getId(),
                user.getId(), user.getEmail(),
                receiverName, receiverAddress, receiverPhone,
                product.getId(), time.getId(), subPitch.getId(),
                bookingDate, type, finalEndDate,
                daysOfWeek, durationMonths,
                breakdown.getTotalPrice(), breakdown.getDepositPrice(), breakdown.getSlots(),
                holdIds);
        data.setEquipmentRentalPrice(equipmentRentalPrice);
        data.setEquipments(equipmentSelections);

        long pendingId = pendingBookingCache.store(data);
        return new PreparedBookingResult(
                pendingId,
                breakdown.getDepositPrice(),
                equipmentRentalPrice,
                breakdown.getDepositPrice() + equipmentRentalPrice);
    }

    private List<PendingBookingData.EquipmentSelectionData> prepareEquipmentSelections(
            Product product,
            List<BookingEquipmentSelection> selectedEquipments) {
        if (selectedEquipments == null || selectedEquipments.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> quantitiesByEquipment = new LinkedHashMap<>();
        for (BookingEquipmentSelection selection : selectedEquipments) {
            if (selection == null || selection.getEquipmentId() == null || selection.getQuantity() <= 0) {
                throw new IllegalArgumentException("Thông tin phụ kiện không hợp lệ.");
            }
            quantitiesByEquipment.merge(
                    selection.getEquipmentId(), selection.getQuantity(), Math::addExact);
        }

        List<PendingBookingData.EquipmentSelectionData> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantitiesByEquipment.entrySet()) {
            Equipment equipment = equipmentRepository.findByIdWithLock(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phụ kiện ID: " + entry.getKey()));
            if (!equipment.isAvailable()
                    || equipment.getProduct() == null
                    || equipment.getProduct().getId() != product.getId()) {
                throw new IllegalArgumentException("Phụ kiện không thuộc sân đang đặt.");
            }
            if (equipment.getRentalPricePerPlay() <= 0) {
                throw new IllegalArgumentException("Phụ kiện chưa được cấu hình giá thuê tại sân.");
            }
            if (equipment.getBookingStockQuantity() < entry.getValue()) {
                throw new BusinessConflictException(
                        "Phụ kiện " + equipment.getName() + " chỉ còn "
                                + equipment.getBookingStockQuantity() + " sản phẩm tại sân.");
            }

            double totalPrice = equipment.getRentalPricePerPlay() * entry.getValue();
            result.add(new PendingBookingData.EquipmentSelectionData(
                    equipment.getId(), entry.getValue(), equipment.getRentalPricePerPlay(), totalPrice));
        }
        return result;
    }

    private void validateBookingSelection(
            Product product,
            SubPitch subPitch,
            AvailableTime availableTime) {
        if (subPitch.getProduct() == null || subPitch.getProduct().getId() != product.getId()) {
            throw new IllegalArgumentException(
                    "Sân phụ ID " + subPitch.getId()
                            + " không thuộc sân ID " + product.getId() + ".");
        }

        if (subPitchAvailableTimeRepository
                .findBySubPitchAndAvailableTime(subPitch, availableTime)
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "Khung giờ ID " + availableTime.getId()
                            + " không được cấu hình cho sân phụ ID " + subPitch.getId() + ".");
        }
    }

    private List<TemporaryBooking> reservePendingBookingSlots(
            SubPitch subPitch,
            AvailableTime availableTime,
            LocalDate anchorDate,
            List<LocalDate> datesToBook,
            Long userId) {

        LinkedHashSet<LocalDate> datesToHold = new LinkedHashSet<>();
        datesToHold.add(anchorDate);
        datesToHold.addAll(datesToBook);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        List<TemporaryBooking> holds = new ArrayList<>();

        for (LocalDate date : datesToHold) {
            Optional<TemporaryBooking> existing = temporaryBookingRepository
                    .findBySubPitchAndAvailableTimeAndBookingDateWithLock(
                            subPitch, availableTime, date);

            TemporaryBooking hold;
            if (existing.isPresent()) {
                hold = existing.get();
                if (hold.isExpired()) {
                    temporaryBookingRepository.delete(hold);
                    temporaryBookingRepository.flush();
                    if (date.equals(anchorDate)) {
                        throw new IllegalArgumentException("Phiên giữ chỗ đã hết hạn.");
                    }
                    hold = createTemporaryBooking(subPitch, availableTime, date, userId);
                } else if (hold.getUserId() == null || !hold.getUserId().equals(userId)) {
                    throw new IllegalArgumentException(
                            "Sân đang được người khác giữ vào ngày "
                                    + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
                }
            } else {
                if (date.equals(anchorDate)) {
                    throw new IllegalArgumentException(
                            "Bạn cần giữ chỗ cho ngày bắt đầu trước khi xác nhận đặt.");
                }
                hold = createTemporaryBooking(subPitch, availableTime, date, userId);
            }

            hold.setHoldExpiresAt(expiresAt);
            holds.add(temporaryBookingRepository.save(hold));
        }

        return holds;
    }

    private TemporaryBooking createTemporaryBooking(
            SubPitch subPitch,
            AvailableTime availableTime,
            LocalDate bookingDate,
            Long userId) {
        TemporaryBooking hold = new TemporaryBooking();
        hold.setSubPitch(subPitch);
        hold.setAvailableTime(availableTime);
        hold.setBookingDate(bookingDate);
        hold.setUserId(userId);
        return hold;
    }

    /**
     * Called on successful VNPay payment. Performs a final collision check then
     * writes the confirmed booking to DB with DA_DAT (đã đặt cọc) status —
     * deposit đã thanh toán, phần còn lại trả tại sân khi staff confirm.
     */
    @Transactional
    public BookingResponseDTO confirmPendingBooking(PendingBookingData data) {
        // Re-fetch entities by id — PendingBookingData stores only IDs to be safely
        // round-tripped through Redis (no JPA proxy / lazy-collection issues).
        User user = userRepository.findUserById(data.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng (id=" + data.getUserId() + ")");
        }
        Product product = productRepository.findById(data.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân (id=" + data.getProductId() + ")"));
        SubPitch subPitch = subPitchRepository.findByIdWithLock(data.getSubPitchId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân phụ (id=" + data.getSubPitchId() + ")"));
        AvailableTime availableTime = timeRepository.findById(data.getAvailableTimeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khung giờ (id=" + data.getAvailableTimeId() + ")"));

        // Final collision check — guard against a race where the hold expired
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            Optional<BookingDetail> conflict = bookingDetailRepository
                    .findBySubPitchAndAvailableTimeAndDate(subPitch, availableTime, slot.getDate());
            if (conflict.isPresent()) {
                log.error("Xung đột lịch sau khi thanh toán thành công: SubPitch={}, time={}, date={}",
                        subPitch.getId(), availableTime.getId(), slot.getDate());
                throw new BusinessConflictException("Sân đã bị đặt bởi người khác trong lúc thanh toán (ngày "
                        + slot.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
            }
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setReceiverName(data.getReceiverName());
        booking.setReceiverAddress(data.getReceiverAddress());
        booking.setReceiverPhone(data.getReceiverPhone());
        booking.setAvailableTime(availableTime);
        booking.setBookingDate(data.getFirstBookingDate());
        booking.setBookingType(data.getBookingType());
        booking.setRecurringEndDate(data.getRecurringEndDate());
        booking.setDaysOfWeek(data.getDaysOfWeek() != null ? data.getDaysOfWeek().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) : null);
        booking.setDurationMonths(data.getDurationMonths());
        booking.setDepositPrice(data.getDepositPrice());
        booking.setTotalPrice(data.getTotalBookingPrice() + data.getEquipmentRentalPrice());
        booking.setStatus(BookingStatus.DA_DAT);

        Booking savedBooking = bookingRepository.save(booking);

        List<BookingDetail> details = new ArrayList<>();
        for (PendingBookingData.SlotData slot : data.getSlots()) {
            BookingDetail detail = new BookingDetail();
            detail.setBooking(savedBooking);
            detail.setProduct(product);
            detail.setPrice(slot.getPrice());
            detail.setSubPitch(subPitch);
            detail.setDate(slot.getDate());
            detail.setSale(slot.getSale());
            detail.setAvailableTime(availableTime);
            details.add(detail);
        }
        bookingDetailRepository.saveAll(details);

        createBundledEquipmentRentals(data, savedBooking, product, user);

        releaseTemporaryBookings(data);

        return bookingMapper.toDTO(savedBooking);
    }

    private void createBundledEquipmentRentals(
            PendingBookingData data,
            Booking booking,
            Product product,
            User user) {
        if (data.getEquipments() == null || data.getEquipments().isEmpty()) {
            return;
        }

        String latestRentalCode = Booking.NO_RENTAL;
        for (PendingBookingData.EquipmentSelectionData selection : data.getEquipments()) {
            Equipment equipment = equipmentRepository.findByIdWithLock(selection.getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy phụ kiện ID: " + selection.getEquipmentId()));
            if (!equipment.isAvailable()
                    || equipment.getProduct() == null
                    || equipment.getProduct().getId() != product.getId()) {
                throw new BusinessConflictException("Phụ kiện không còn thuộc sân đang đặt.");
            }
            if (equipment.getBookingStockQuantity() < selection.getQuantity()) {
                throw new BusinessConflictException(
                        "Phụ kiện " + equipment.getName() + " không còn đủ số lượng sau khi thanh toán.");
            }

            equipment.setBookingStockQuantity(
                    equipment.getBookingStockQuantity() - selection.getQuantity());
            equipmentRepository.save(equipment);

            RentalTool rental = new RentalTool();
            rental.setFullName(data.getReceiverName());
            rental.setEmail(data.getUserEmail());
            rental.setPhone(data.getReceiverPhone());
            rental.setType(RentalType.ON_SITE);
            rental.setBookingId(String.valueOf(booking.getId()));
            rental.setEquipmentId(equipment.getId());
            rental.setProductId(product.getId());
            rental.setPrice(equipment.getPrice() * selection.getQuantity());
            rental.setRentalPrice(selection.getTotalPrice());
            rental.setStatus(RentalToolStatus.PENDING);
            rental.setPaymentStatus(RentalPaymentStatus.PAID);
            rental.setQuantity(selection.getQuantity());
            rental.setRentalDate(data.getFirstBookingDate());
            rental.setCreateAt(LocalDateTime.now());
            rental.setUpdateAt(LocalDateTime.now());
            rental.setOnSiteStockReserved(true);
            rental.setUserId(user.getId());
            RentalTool savedRental = rentalToolRepository.save(rental);
            latestRentalCode = savedRental.getRentalToolCode();
        }

        booking.setRentalToolCode(latestRentalCode);
        bookingRepository.save(booking);
    }

    /**
     * Called on failed VNPay payment. Frees the slot hold and removes the pending booking.
     */
    public void cancelPendingBooking(PendingBookingData data) {
        releaseTemporaryBookings(data);
    }

    private void releaseTemporaryBookings(PendingBookingData data) {
        if (data.getTemporaryBookingIds() != null && !data.getTemporaryBookingIds().isEmpty()) {
            temporaryBookingRepository.deleteAllById(data.getTemporaryBookingIds());
        } else if (data.getTemporaryBookingId() != null) {
            temporaryBookingRepository.deleteById(data.getTemporaryBookingId());
        }
    }

    @Transactional
    public Page<BookingResponseDTO> fetchBookingByUserWithPaging(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(bookingMapper::toDTO);
    }

    /**
     * @deprecated Dùng {@link #cancelByUser(long, long, String)} thay thế.
     *
     * <p>Method này chỉ là thin delegate giữ lại cho legacy {@code DELETE /{id}}
     * endpoint, không hỗ trợ {@code reason} và quan trọng hơn là <b>nuốt</b>
     * {@link com.pitchbooking.app.domain.dto.CancelBookingResponse} mà
     * {@code cancelByUser} trả ra — caller mất luôn refund amount, refund
     * status, used/total sessions, contact info để hiển thị cho user.
     *
     * <p>Caller mới nên gọi {@code cancelByUser(bookingId, user.getId(), reason)}
     * trực tiếp và dùng response để build notification + UI feedback.
     */
    @Deprecated
    @Transactional
    public void cancelBooking(long bookingId, User user) {
        cancelByUser(bookingId, user.getId(), null);
    }

    /**
     * Cancel a booking on behalf of its owner. Implements CANCEL_BOOKING_FEATURE
     * decisions D0.1–D0.4: status-gate, 2h gate for ONE_TIME, proportional refund
     * for WEEKLY, cascade ON_SITE rental → CANCELLED + restock.
     *
     * <p>Slot release is implicit: the slot-availability queries in
     * BookingDetailRepository exclude DA_HUY bookings, so we don't delete the
     * BookingDetail rows (we need them to compute usedSessions and as audit).
     *
     * <p>Notifications are produced by callers in Phase 4 — keep this method
     * dependency-free of NotificationService to avoid a tx cycle.
     */
    @Transactional
    public CancelBookingResponse cancelByUser(long bookingId, long userId, String reason) {
        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + bookingId));

        // E6 — ownership
        if (booking.getUser() == null || booking.getUser().getId() != userId) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn đặt sân này.");
        }

        // E1 — already cancelled → 409 (caller may map this exception)
        if (booking.getStatus() == BookingStatus.DA_HUY) {
            throw new BusinessConflictException("Đơn đặt sân này đã được hủy trước đó.");
        }

        // D0.1 — only cancellable while CHO_THANH_TOAN or DA_DAT.
        // E2 — DA_THANH_TOAN is terminal; reject.
        if (booking.getStatus() != BookingStatus.CHO_THANH_TOAN
                && booking.getStatus() != BookingStatus.DA_DAT) {
            throw new BusinessConflictException("Đơn đã hoàn thành hoặc không ở trạng thái cho phép huỷ.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Compute refund per booking type (D0.2, D0.3, E3, E4)
        double refundAmount;
        int totalSessions;
        int usedSessions;

        if (booking.getBookingType() == BookingType.WEEKLY_RECURRING) {
            List<BookingDetail> details = booking.getBookingDetails() != null
                    ? booking.getBookingDetails() : List.of();
            totalSessions = details.size();
            usedSessions = (int) details.stream()
                    .filter(d -> d.getDate() != null && d.getDate().isBefore(today))
                    .count();
            int remaining = totalSessions - usedSessions;

            if (totalSessions == 0 || remaining <= 0) {
                // E4 — all sessions already passed; allow cancel but no refund.
                refundAmount = 0;
            } else {
                refundAmount = booking.getDepositPrice() * ((double) remaining / totalSessions);
            }
        } else {
            // ONE_TIME — totalSessions=1, usedSessions always 0 at cancel time.
            totalSessions = 1;
            usedSessions = 0;

            // E3 — 2h gate.
            LocalTime slotTime = booking.getAvailableTime() != null && booking.getAvailableTime().getTime() != null
                    ? booking.getAvailableTime().getTime()
                    : LocalTime.MIDNIGHT;
            LocalDateTime slotStart = booking.getBookingDate().atTime(slotTime);
            if (now.isAfter(slotStart.minusHours(2))) {
                long minutesUntilSlot = java.time.Duration.between(now, slotStart).toMinutes();
                String suffix = minutesUntilSlot > 0
                        ? " (còn " + minutesUntilSlot + " phút trước giờ bắt đầu)."
                        : ".";
                throw new IllegalArgumentException(
                        "Không thể huỷ trong vòng 2 tiếng trước giờ bắt đầu" + suffix);
            }
            refundAmount = booking.getDepositPrice();
        }

        // Cascade rental đính kèm booking (CANCEL_BOOKING_FEATURE §D0.4 + §E7):
        //   - ON_SITE (bundled): set CANCELLED + trả Equipment.bookingStockQuantity.
        //     Lý do: ON_SITE trừ booking-level stock lúc confirm, huỷ → restore.
        //   - DAILY: theo §E7, DAILY là standalone — KHÔNG gắn bookingId
        //     (RentalToolService.handleSubmitRental không bao giờ set bookingId
        //     cho DAILY; chỉ ON_SITE path qua handleOnSiteRental mới set). Vậy
        //     DAILY xuất hiện trong list này = data invariant đã vỡ.
        //
        //     PAID là case gây thiệt hại: tồn kho đã reserve trong
        //     EquipmentStockByDate cho từng ngày của rental period. Cascade thủ
        //     công ở đây sẽ phải replicate logic ngược của completeRental
        //     (availableStock += qty, reservedStock -= qty cho mỗi ngày) —
        //     nhưng đó là business rule khác hẳn (DAILY chưa bao giờ thuộc
        //     phạm vi cancel-booking). Throw thay vì âm thầm bỏ qua để corrupt
        //     EquipmentStockByDate không xảy ra; sự cố phải nổi lên để fix
        //     root cause (code nào đã gán bookingId cho DAILY?).
        //
        //     PENDING/COMPLETED/CANCELLED của DAILY: stock không ở trạng thái
        //     reserved-vì-booking-này, bỏ qua an toàn.
        List<RentalTool> bundled = rentalToolRepository.findRentalToolsByBookingId(String.valueOf(booking.getId()));
        for (RentalTool rt : bundled) {
            if (rt.getType() == RentalType.DAILY) {
                if (rt.isDailyStockReserved()) {
                    throw new IllegalStateException(
                            "DAILY rental đang giữ tồn kho không được phép gắn booking (vi phạm invariant unbundled §E7). "
                                    + "RentalTool id=" + rt.getId() + ", bookingId=" + rt.getBookingId()
                                    + ". Cần điều tra code path nào đã gán bookingId trước khi cho phép huỷ booking này.");
                }
                continue;
            }
            if (rt.getType() != RentalType.ON_SITE) continue;
            if (rt.getStatus() == RentalToolStatus.CANCELLED) continue;
            rt.setStatus(RentalToolStatus.CANCELLED);
            rt.setUpdateAt(now);
            if (rt.isOnSiteStockReserved()) {
                Equipment equipment = equipmentRepository.findByIdWithLock(rt.getEquipmentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy thiết bị ID: " + rt.getEquipmentId()));
                equipment.setBookingStockQuantity(
                        equipment.getBookingStockQuantity() + rt.getQuantity());
                equipmentRepository.save(equipment);
                rt.setOnSiteStockReserved(false);
            }
            rentalToolRepository.save(rt);
        }

        // Update booking
        booking.setStatus(BookingStatus.DA_HUY);
        booking.setCancelledAt(now);
        booking.setCancelReason(reason);
        booking.setRefundAmount(refundAmount);
        booking.setUsedSessionsAtCancel(usedSessions);
        booking.setTotalSessionsAtCancel(totalSessions);
        booking.setRefundStatus(refundAmount > 0 ? RefundStatus.PENDING_REFUND : RefundStatus.NOT_APPLICABLE);
        bookingRepository.save(booking);

        log.info("User {} cancelled booking {} — refund={}, used={}/{}",
                userId, bookingId, refundAmount, usedSessions, totalSessions);

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .refundStatus(booking.getRefundStatus())
                .refundAmount(refundAmount)
                .usedSessions(usedSessions)
                .totalSessions(totalSessions)
                .cancelledAt(now)
                .contactHotline(contactProperties.getHotline())
                .contactEmail(contactProperties.getEmail())
                .build();
    }

    /**
     * Admin marks a pending refund as paid. Idempotent (E5): a second call when
     * already REFUNDED throws — caller should map to 409.
     */
    @Transactional
    public CancelBookingResponse confirmRefund(long bookingId) {
        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt sân ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.DA_HUY) {
            throw new BusinessConflictException("Chỉ huỷ rồi mới hoàn cọc được.");
        }
        if (booking.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new BusinessConflictException("Trạng thái hoàn cọc hiện tại không hợp lệ: "
                    + booking.getRefundStatus());
        }

        booking.setRefundStatus(RefundStatus.REFUNDED);
        bookingRepository.save(booking);

        log.info("Admin confirmed refund for booking {}", bookingId);

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .refundStatus(booking.getRefundStatus())
                .refundAmount(booking.getRefundAmount() != null ? booking.getRefundAmount() : 0)
                .usedSessions(booking.getUsedSessionsAtCancel())
                .totalSessions(booking.getTotalSessionsAtCancel())
                .cancelledAt(booking.getCancelledAt())
                .contactHotline(contactProperties.getHotline())
                .contactEmail(contactProperties.getEmail())
                .build();
    }
}
