package com.pitchbooking.app.controller.client;

import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.NotificationDTO;
import com.pitchbooking.app.service.NotificationService;
import com.pitchbooking.app.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/notifications")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationController {

    NotificationService notificationService;
    SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = securityUtils.getCurrentUser();
        Page<NotificationDTO> data = notificationService.listByUser(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.<Page<NotificationDTO>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> unreadCount() {
        User user = securityUtils.getCurrentUser();
        int count = notificationService.countUnread(user.getId());
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder()
                .status(200).message("Thành công")
                .data(Map.of("unread", count)).build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        User user = securityUtils.getCurrentUser();
        notificationService.markRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Đã đánh dấu đọc").build());
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead() {
        User user = securityUtils.getCurrentUser();
        int updated = notificationService.markAllRead(user.getId());
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder()
                .status(200).message("Đã đánh dấu tất cả")
                .data(Map.of("updated", updated)).build());
    }
}
