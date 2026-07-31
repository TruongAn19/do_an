package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.ProductUpsertRequest;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.service.BookingStatsService;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductController {

    ProductService productService;
    UploadService uploadService;
    BookingStatsService bookingStatsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProducts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", required = false) String searchTerm) {

        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<ProductResponseDTO> mainProducts;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            mainProducts = productService.findByNameContaining(searchTerm, pageable);
        } else {
            mainProducts = productService.getAllProductAdmin(pageable);
        }

        Map<String, Object> result = Map.of(
                "products", mainProducts.getContent(),
                "currentPage", page,
                "totalPages", mainProducts.getTotalPages(),
                "totalElements", mainProducts.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @RequestPart("product") ProductUpsertRequest request,
            @RequestPart(value = "productImg", required = false) MultipartFile file) {

        Product product = new Product();
        applyCreateRequest(product, request);
        if (file != null && !file.isEmpty()) {
            product.setImage(uploadService.handleSaveUploadFile(file, "product"));
        }
        product.setStatus("ACTIVE");
        ProductResponseDTO savedProduct = productService.handSaveProduct(product);

        return ResponseEntity.ok(ApiResponse.<ProductResponseDTO>builder()
                .status(200).message("Tạo sân thành công").data(savedProduct).build());
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getProductOptions() {
        return ResponseEntity.ok(ApiResponse.<List<ProductResponseDTO>>builder()
                .status(200)
                .message("Thành công")
                .data(productService.getAllProductOptions())
                .build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProduct(@PathVariable long productId) {
        ProductResponseDTO product = productService.getProductByID(productId);
        return ResponseEntity.ok(ApiResponse.<ProductResponseDTO>builder()
                .status(200).message("Thành công").data(product).build());
    }

    @PutMapping(value = "/{productId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable long productId,
            @Valid @RequestPart("product") ProductUpsertRequest request,
            @RequestPart(value = "productImg", required = false) MultipartFile file) {

        Product existing = productService.getRawProductById(productId);
        existing.setName(request.getName().trim());
        existing.setDetailDesc(normalizeDescription(request.getDetailDesc()));
        existing.setAddress(request.getAddress());
        existing.setAddressDetail(request.getAddressDetail());
        existing.setSale(request.getSale());
        existing.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setShortDesc(request.getShortDesc());
        existing.setPitchType(request.getPitchType());

        if (file != null && !file.isEmpty()) {
            existing.setImage(uploadService.handleSaveUploadFile(file, "product"));
        } else if (request.getImage() != null && !request.getImage().isEmpty()) {
            existing.setImage(request.getImage());
        }

        ProductResponseDTO updatedProduct = productService.handSaveProduct(existing);

        return ResponseEntity.ok(ApiResponse.<ProductResponseDTO>builder()
                .status(200).message("Cập nhật sân thành công").data(updatedProduct).build());
    }

    private void applyCreateRequest(Product product, ProductUpsertRequest request) {
        product.setName(request.getName().trim());
        product.setDetailDesc(normalizeDescription(request.getDetailDesc()));
        product.setAddress(request.getAddress());
        product.setAddressDetail(request.getAddressDetail());
        product.setSale(request.getSale());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setShortDesc(request.getShortDesc());
        product.setPitchType(request.getPitchType());
        product.setSubPitchNames(request.getSubPitchNames());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            product.setImage(request.getImage());
        }
    }

    private String normalizeDescription(String description) {
        return description.replace("\n", "<br>");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable long productId) {
        productService.deleteAllProduct(productId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200).message("Xóa sân thành công").data(null).build());
    }

    @GetMapping("/statistics/revenue")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Double> revenueData = bookingStatsService.getRevenueBetweenDates(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.<Map<String, Double>>builder()
                .status(200).message("Thành công").data(revenueData).build());
    }
}
