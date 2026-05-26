package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.Equipment;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.ProductCriteriaDTO;
import com.pitchbooking.app.domain.dto.ProductResponseDTO;
import com.pitchbooking.app.service.ProductService;
import com.pitchbooking.app.service.EquipmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    ProductService productService;
    EquipmentService equipmentService;

    @GetMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProducts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "price", required = false) Double maxPrice,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        Pageable pageable = PageRequest.of(page - 1, 6);
        
        if (sort != null && !sort.trim().isEmpty()) {
            if ("gia-tang-dan".equals(sort) || "pricePerHour,asc".equals(sort)) {
                pageable = PageRequest.of(page - 1, 6, Sort.by("price").ascending());
            } else if ("gia-giam-dan".equals(sort) || "pricePerHour,desc".equals(sort)) {
                pageable = PageRequest.of(page - 1, 6, Sort.by("price").descending());
            }
        }

        Page<ProductResponseDTO> mainProduct = productService.searchProducts(search, address, maxPrice, pageable);

        Map<String, Object> result = Map.of(
                "products", mainProduct.getContent(),
                "currentPage", page,
                "totalPages", Math.max(mainProduct.getTotalPages(), 0),
                "totalElements", mainProduct.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProduct(@PathVariable long productId) {
        ProductResponseDTO product = productService.getProductByID(productId);
        List<AvailableTime> availableTime = productService.getAllTime();
        double discountPrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        List<Equipment> equipments = equipmentService.getEquipmentsByProductId(productId);

        Map<String, Object> data = Map.of(
                "product", product,
                "availableTime", availableTime,
                "discountPrice", discountPrice,
                "equipments", equipments);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping("/api/v1/equipments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEquipments(
            @RequestParam(value = "factory", required = false) String[] factories,
            @RequestParam(value = "price", required = false) String[] prices,
            @RequestParam(value = "sort", required = false, defaultValue = "gia-nothing") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        int size = 6;
        Pageable pageable = PageRequest.of(page, size);
        List<String> factoryList = (factories != null && factories.length > 0) ? Arrays.asList(factories) : null;
        List<String> priceList = (prices != null && prices.length > 0) ? Arrays.asList(prices) : null;

        Page<Equipment> equipmentPage = equipmentService.getEquipments(factoryList, priceList, sort, pageable);

        Map<String, Object> result = Map.of(
                "equipments", equipmentPage.getContent(),
                "currentPage", page,
                "totalPages", equipmentPage.getTotalPages(),
                "totalElements", equipmentPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/equipments/{equipmentId}")
    public ResponseEntity<ApiResponse<Equipment>> getEquipment(@PathVariable long equipmentId) {
        Equipment equipment = equipmentService.getEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị id=" + equipmentId));
        return ResponseEntity.ok(ApiResponse.<Equipment>builder()
                .status(200).message("Thành công").data(equipment).build());
    }
}
