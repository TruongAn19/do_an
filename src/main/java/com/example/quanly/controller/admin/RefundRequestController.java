package com.example.quanly.controller.admin;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/refund-requests")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RefundRequestController {

    BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRefundRequests(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("cancelledAt").descending());
        Page<BookingResponseDTO> result = bookingService.fetchRefundRequests(status, pageable);

        Map<String, Object> data = Map.of(
                "items", result.getContent(),
                "currentPage", page,
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
