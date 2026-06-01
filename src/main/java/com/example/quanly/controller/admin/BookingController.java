package com.example.quanly.controller.admin;

import com.example.quanly.domain.RentalTool;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.BookingResponseDTO;
import com.example.quanly.service.BookingService;
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
        public ResponseEntity<ApiResponse<String>> deleteBooking(@PathVariable long id) {
                bookingService.deleteBookingById(id);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200)
                                .message("Xóa booking thành công")
                                .data(null)
                                .build());
        }

        @PutMapping("/{id}/refund")
        public ResponseEntity<ApiResponse<Void>> confirmRefund(@PathVariable long id) {
                bookingService.confirmRefund(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(200)
                                .message("Đã xác nhận hoàn cọc")
                                .data(null)
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
}
