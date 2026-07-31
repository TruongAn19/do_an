package com.example.quanly.controller;

import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.NotificationDTO;
import com.example.quanly.service.NotificationService;
import com.example.quanly.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationController {

    NotificationService notificationService;
    SecurityUtils securityUtils;

    // ---------------- Client ----------------

    @GetMapping("/client/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> listClient(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtils.getCurrentUser().getId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(ApiResponse.<Page<NotificationDTO>>builder()
                .status(200).message("Thành công")
                .data(notificationService.listByUser(userId, pageable)).build());
    }

    @GetMapping("/client/notifications/unread-count")
    public ResponseEntity<ApiResponse<Integer>> unreadCount() {
        Long userId = securityUtils.getCurrentUser().getId();
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .status(200).message("Thành công")
                .data(notificationService.countUnread(userId)).build());
    }

    @PutMapping("/client/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUser().getId();
        notificationService.markRead(id, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Đã đọc").data(null).build());
    }

    @PutMapping("/client/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        Long userId = securityUtils.getCurrentUser().getId();
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Đã đọc tất cả").data(null).build());
    }

    // ---------------- Admin / Staff ----------------

    @GetMapping("/admin/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> listAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User current = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(ApiResponse.<Page<NotificationDTO>>builder()
                .status(200).message("Thành công")
                .data(notificationService.listByUser(current.getId(), pageable)).build());
    }
}
