package com.example.quanly.controller.admin;

import com.example.quanly.domain.Racket;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RacketStockByDateService;
import com.example.quanly.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RacketController {

    private final RacketService racketService;
    private final UploadService uploadService;
    private final RacketStockByDateService racketStockByDateService;

    @GetMapping("/api/v1/admin/rackets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRackets(
            @RequestParam(value = "page", defaultValue = "1") int page) {

        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Racket> byProducts = racketService.getAllRacket(pageable);

        Map<String, Object> result = Map.of(
                "rackets", byProducts.getContent(),
                "currentPage", page,
                "totalPages", byProducts.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/admin/rackets/{racketId}")
    public ResponseEntity<ApiResponse<Racket>> getRacketDetail(@PathVariable long racketId) {
        Racket racket = racketService.getRacketById(racketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vợt id=" + racketId));
        return ResponseEntity.ok(ApiResponse.<Racket>builder()
                .status(200).message("Thành công").data(racket).build());
    }

    @PostMapping(value = "/api/v1/admin/rackets", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Racket>> createRacket(
            @RequestPart("racket") Racket racket,
            @RequestPart(value = "racketImg", required = false) MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            racket.setImage(uploadService.handleSaveUploadFile(file, "racket"));
        }
        racket.setStatus("AVAILABLE");
        Racket saved = racketService.handSaveRacket(racket);
        racketStockByDateService.generateStockForRacket(saved);

        return ResponseEntity.ok(ApiResponse.<Racket>builder()
                .status(200).message("Tạo vợt thành công").data(saved).build());
    }

    @PutMapping(value = "/api/v1/admin/rackets/{racketId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Racket>> updateRacket(
            @PathVariable long racketId,
            @RequestPart("racket") Racket racket,
            @RequestPart(value = "racketImg", required = false) MultipartFile file) {

        Racket existing = racketService.getRacketById(racketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vợt id=" + racketId));

        existing.setName(racket.getName());
        existing.setPrice(racket.getPrice());
        existing.setFactory(racket.getFactory());
        existing.setAvailable(racket.isAvailable());
        existing.setRentalPricePerDay(racket.getRentalPricePerDay());
        existing.setRentalPricePerPlay(racket.getRentalPricePerPlay());
        existing.setBookingStockQuantity(racket.getBookingStockQuantity());
        existing.setQuantity(racket.getQuantity());
        existing.setStatus(racket.getStatus());
        existing.setProduct(racket.getProduct());
        if (file != null && !file.isEmpty()) {
            existing.setImage(uploadService.handleSaveUploadFile(file, "racket"));
        }
        racketService.handSaveRacket(existing);

        return ResponseEntity.ok(ApiResponse.<Racket>builder()
                .status(200).message("Cập nhật vợt thành công").data(existing).build());
    }
}
