package com.example.quanly.controller;

import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.NotificationDTO;
import com.example.quanly.service.NotificationService;
import com.example.quanly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping({"/client/notifications", "/admin/notifications"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = userService.getUserByEmail(principal.getName()).getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> p = notificationService.listByUser(userId, pageable);
        long unread = notificationService.countUnread(userId);

        Map<String, Object> data = Map.of(
                "notifications", p.getContent(),
                "totalPages", p.getTotalPages(),
                "currentPage", p.getNumber(),
                "totalElements", p.getTotalElements(),
                "unreadCount", unread);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200).message("Thành công").data(data).build());
    }

    @GetMapping({"/client/notifications/unread-count", "/admin/notifications/unread-count"})
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(Principal principal) {
        long userId = userService.getUserByEmail(principal.getName()).getId();
        long unread = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder()
                .status(200).message("Thành công").data(Map.of("unreadCount", unread)).build());
    }

    @PutMapping({"/client/notifications/{id}/read", "/admin/notifications/{id}/read"})
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable long id, Principal principal) {
        long userId = userService.getUserByEmail(principal.getName()).getId();
        notificationService.markRead(id, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200).message("Đã đánh dấu đã đọc").build());
    }

    @PutMapping({"/client/notifications/read-all", "/admin/notifications/read-all"})
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(Principal principal) {
        long userId = userService.getUserByEmail(principal.getName()).getId();
        int updated = notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder()
                .status(200).message("Đã đánh dấu tất cả là đã đọc")
                .data(Map.of("updatedCount", updated)).build());
    }
}
