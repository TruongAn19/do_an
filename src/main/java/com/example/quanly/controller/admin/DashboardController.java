package com.example.quanly.controller.admin;

import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.EquipmentService;
import com.example.quanly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class DashboardController {
    private final UserService userService;
    private final EquipmentService equipmentService;
    private final ProductService productService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> data = Map.of(
                "countUser", userService.countUser(),
                "countProduct", productService.getCourtProduct(),
                "countByEquipment", equipmentService.countEquipment()
        );
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }
}
