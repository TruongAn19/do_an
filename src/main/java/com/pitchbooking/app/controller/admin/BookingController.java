package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.Booking;
import com.pitchbooking.app.domain.NotificationType;
import com.pitchbooking.app.domain.RefundStatus;
import com.pitchbooking.app.domain.RentalTool;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.BookingResponseDTO;
import com.pitchbooking.app.domain.dto.CancelBookingResponse;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.mapper.BookingMapper;
import com.pitchbooking.app.repository.BookingRepository;
import com.pitchbooking.app.service.BookingService;
import com.pitchbooking.app.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingController {
        BookingService bookingService;
        BookingRepository bookingRepository;
        BookingMapper bookingMapper;
        NotificationService notificationService;

        @GetMapping
        public ResponseEntity<ApiResponse<Map<String, Object>>> getBookings(
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                        @RequestParam(value = "search", required = false) String searchTerm,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {

                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                Page<BookingResponseDTO> bookingPage;

                if (date != null) {
                        bookingPage = bookingService.fetchBookingsByDate(date, pageable);
                } else if (searchTerm != null && !searchTerm.isEmpty()) {
                        bookingPage = bookingService.fetchBookingCode(searchTerm, pageable);
                } else {
                        bookingPage = bookingService.fetchAllBookings(pageable);
                }

                Map<String, Object> result = Map.of(
                                "bookings", bookingPage.getContent(),
                                "currentPage", page,
                                "totalPages", bookingPage.getTotalPages(),
                                "totalElements", bookingPage.getTotalElements());

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200)
                                .message("Thành công")
                                .data(result)
                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingDetail(@PathVariable long id) {
                BookingResponseDTO bookingDTO = bookingService.fetchBookingById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking id=" + id));
                List<RentalTool> rentalTools = bookingService.getRentalToolsByBookingId(id);

                Map<String, Object> result = Map.of(
                                "booking", bookingDTO,
                                "rentalTools", rentalTools);

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200)
                                .message("Thành công")
                                .data(result)
                                .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<CancelBookingResponse>> deleteBooking(@PathVariable long id) {
                CancelBookingResponse cancelled = bookingService.deleteBookingById(id);
                return ResponseEntity.ok(ApiResponse.<CancelBookingResponse>builder()
                                .status(200)
                                .message("Hủy booking thành công")
                                .data(cancelled)
                                .build());
        }

        @PutMapping("/{id}/status")
        public ResponseEntity<ApiResponse<BookingResponseDTO>> updateBookingStatus(
                        @PathVariable long id,
                        @RequestBody Map<String, String> body) {
                String status = body.get("status");
                bookingService.updateBooking(id, status);
                BookingResponseDTO updatedBooking = bookingService.fetchBookingById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking sau cập nhật"));

                return ResponseEntity.ok(ApiResponse.<BookingResponseDTO>builder()
                                .status(200)
                                .message("Cập nhật trạng thái thành công")
                                .data(updatedBooking)
                                .build());
        }

        /**
         * Admin marks a deposit as refunded (D0.7 — manual transfer, no VNPay API).
         * Fires a REFUND_DONE notification + WS push to the original booking owner.
         */
        @PutMapping("/{id}/refund")
        public ResponseEntity<ApiResponse<CancelBookingResponse>> confirmRefund(@PathVariable long id) {
                CancelBookingResponse resp = bookingService.confirmRefund(id);

                // Notify the booking owner.
                Booking booking = bookingRepository.findById(id).orElse(null);
                if (booking != null && booking.getUser() != null) {
                        String msg = String.format(
                                        "Quản trị viên đã xác nhận hoàn cọc %,.0fđ cho đơn #%d. "
                                        + "Vui lòng kiểm tra tài khoản nhận tiền.",
                                        resp.getRefundAmount(), id);
                        NotificationDTO dto = notificationService.create(
                                        booking.getUser().getId(),
                                        NotificationType.REFUND_DONE,
                                        "BOOKING", id,
                                        "Đã hoàn cọc",
                                        msg);
                        notificationService.pushToUser(booking.getUser().getId(), dto);
                }

                return ResponseEntity.ok(ApiResponse.<CancelBookingResponse>builder()
                                .status(200).message("Đã đánh dấu đã hoàn cọc").data(resp).build());
        }

        /**
         * List bookings filtered by refund status — feeds the admin refund-requests page.
         * Pass {@code status} ∈ {PENDING_REFUND, REFUNDED} or omit for all DA_HUY bookings.
         */
        @GetMapping("/refund-requests")
        public ResponseEntity<ApiResponse<Map<String, Object>>> listRefundRequests(
                        @RequestParam(required = false) RefundStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("cancelledAt").descending());
                Page<Booking> result = (status != null)
                                ? bookingRepository.findByRefundStatus(status, pageable)
                                : bookingRepository.findByStatus(com.pitchbooking.app.domain.BookingStatus.DA_HUY, pageable);

                List<BookingResponseDTO> dtoList = result.getContent().stream()
                                .map(bookingMapper::toDTO)
                                .toList();

                Map<String, Object> data = Map.of(
                                "bookings", dtoList,
                                "currentPage", page,
                                "totalPages", result.getTotalPages(),
                                "totalElements", result.getTotalElements());
                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }
}
