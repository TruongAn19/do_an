package com.example.quanly.controller.client;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.domain.dto.ProductResponseDTO;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
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
    RacketService racketService;

    @GetMapping("/api/v1/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProducts(
            ProductCriteriaDTO productCriteriaDTO,
            @RequestParam(value = "search", required = false) String searchTerm) {

        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            }
        } catch (Exception ignored) {
        }

        Pageable pageable = PageRequest.of(page - 1, 6);
        Optional<String> sortOpt = productCriteriaDTO.getSort();
        if (sortOpt != null && sortOpt.isPresent()) {
            String sort = sortOpt.get();
            if ("gia-tang-dan".equals(sort)) {
                pageable = PageRequest.of(page - 1, 6, Sort.by("price").ascending());
            } else if ("gia-giam-dan".equals(sort)) {
                pageable = PageRequest.of(page - 1, 6, Sort.by("price").descending());
            }
        }

        Page<ProductResponseDTO> mainProduct;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            mainProduct = productService.findByNameContaining(searchTerm, pageable);
        } else if (sortOpt != null && sortOpt.isPresent()
                && (productCriteriaDTO.getAddress() != null || productCriteriaDTO.getPrice() != null)) {
            mainProduct = productService.getAllProductWithSpec(pageable, productCriteriaDTO);
        } else {
            mainProduct = productService.getAllProductClient(pageable);
        }

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

        Map<String, Object> data = Map.of(
                "product", product,
                "availableTime", availableTime,
                "discountPrice", discountPrice);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping("/api/v1/rackets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRackets(
            @RequestParam(value = "factory", required = false) String[] factories,
            @RequestParam(value = "price", required = false) String[] prices,
            @RequestParam(value = "sort", required = false, defaultValue = "gia-nothing") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        int size = 6;
        Pageable pageable = PageRequest.of(page, size);
        List<String> factoryList = (factories != null && factories.length > 0) ? Arrays.asList(factories) : null;
        List<String> priceList = (prices != null && prices.length > 0) ? Arrays.asList(prices) : null;

        Page<Racket> racketPage = racketService.getRackets(factoryList, priceList, sort, pageable);

        Map<String, Object> result = Map.of(
                "rackets", racketPage.getContent(),
                "currentPage", page,
                "totalPages", racketPage.getTotalPages(),
                "totalElements", racketPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(result).build());
    }

    @GetMapping("/api/v1/rackets/{racketId}")
    public ResponseEntity<ApiResponse<Racket>> getRacket(@PathVariable long racketId) {
        Racket racket = racketService.getRacketById(racketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vợt id=" + racketId));
        return ResponseEntity.ok(ApiResponse.<Racket>builder()
                .status(200).message("Thành công").data(racket).build());
    }
}
