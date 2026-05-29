package com.pitchbooking.app.controller.admin;

import com.pitchbooking.app.domain.PitchType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;
import com.pitchbooking.app.domain.SubPitchAvailableTime;
import com.pitchbooking.app.domain.AvailableTime;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.SubPitchDTO;
import com.pitchbooking.app.exception.ResourceNotFoundException;
import com.pitchbooking.app.repository.ProductRepository;
import com.pitchbooking.app.repository.SubPitchAvailableTimeRepository;
import com.pitchbooking.app.repository.SubPitchRepository;
import com.pitchbooking.app.repository.TimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sub-pitches")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubPitchController {

    SubPitchRepository subPitchRepository;
    ProductRepository productRepository;
    TimeRepository timeRepository;
    SubPitchAvailableTimeRepository subPitchAvailableTimeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubPitchDTO>>> list(@RequestParam Long productId) {
        List<SubPitchDTO> data = subPitchRepository.findByProductId(productId).stream()
                .map(SubPitchController::toDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.<List<SubPitchDTO>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubPitchDTO>> create(@RequestBody SubPitchDTO req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm ID: " + req.getProductId()));

        SubPitch sp = new SubPitch();
        sp.setName(req.getName());
        sp.setPitchType(req.getPitchType() != null ? req.getPitchType() : PitchType.FIVE_ASIDE);
        sp.setProduct(product);
        SubPitch saved = subPitchRepository.save(sp);

        // Liên kết sub-pitch với toàn bộ khung giờ hệ thống
        for (AvailableTime t : timeRepository.findAll()) {
            SubPitchAvailableTime sat = new SubPitchAvailableTime();
            sat.setSubPitch(saved);
            sat.setAvailableTime(t);
            subPitchAvailableTimeRepository.save(sat);
        }

        return ResponseEntity.ok(ApiResponse.<SubPitchDTO>builder()
                .status(200).message("Tạo sân con thành công").data(toDTO(saved)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubPitchDTO>> update(@PathVariable Long id, @RequestBody SubPitchDTO req) {
        SubPitch sp = subPitchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân con ID: " + id));
        if (req.getName() != null) sp.setName(req.getName());
        if (req.getPitchType() != null) sp.setPitchType(req.getPitchType());
        SubPitch saved = subPitchRepository.save(sp);
        return ResponseEntity.ok(ApiResponse.<SubPitchDTO>builder()
                .status(200).message("Cập nhật sân con thành công").data(toDTO(saved)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subPitchRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Xóa sân con thành công").build());
    }

    private static SubPitchDTO toDTO(SubPitch sp) {
        Long productId = sp.getProduct() != null ? sp.getProduct().getId() : null;
        return new SubPitchDTO(sp.getId(), sp.getName(), sp.getPitchType(), productId);
    }
}
