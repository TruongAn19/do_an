package com.example.quanly.controller.client;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.ChatMessageDto;
import com.example.quanly.domain.dto.MatchPostResponseDTO;
import com.example.quanly.repository.MatchParticipantRepository;
import com.example.quanly.service.AuthenticationFacade;
import com.example.quanly.service.ChatService;
import com.example.quanly.service.MatchPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/match-posts")
public class MatchPostController {
        private final MatchPostService matchPostService;
        private final ChatService chatService;
        private final AuthenticationFacade authenticationFacade;
        private final MatchParticipantRepository matchParticipantRepository;

        @GetMapping
        public ResponseEntity<ApiResponse<Map<String, Object>>> searchPosts(
                        @RequestParam(required = false) String area,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate playDate,
                        @RequestParam(required = false) String skillLevel,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "4") int size) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<MatchPostResponseDTO> posts = (area != null && playDate != null)
                                ? matchPostService.searchPosts(area, playDate, skillLevel, pageable)
                                : matchPostService.getAllPosts(pageable);

                User currentUser = authenticationFacade.getCurrentUser();
                Set<Long> joinedPostIds = currentUser != null
                                ? matchParticipantRepository.findPostIdsByUserId(currentUser.getId())
                                : Collections.emptySet();

                posts.forEach(dto -> {
                        dto.setOwner(currentUser != null && Objects.equals(currentUser.getId(), dto.getUserId()));
                        dto.setJoined(currentUser != null && joinedPostIds.contains(dto.getId()));
                });

                Map<String, Object> result = Map.of(
                                "posts", posts.getContent(),
                                "currentPage", page,
                                "totalPages", posts.getTotalPages(),
                                "totalElements", posts.getTotalElements());

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(result).build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Map<String, Object>>> getPost(@PathVariable Long id) {
                MatchPostResponseDTO post = matchPostService.getPostById(id);
                User currentUser = authenticationFacade.getCurrentUser();
                List<ChatMessageDto> messages = chatService.getMessageDtos(post.getId());

                // Cần Entity để lấy participants nếu DTO chưa có
                MatchPost entity = matchPostService.getPostEntityById(id);
                boolean alreadyJoined = currentUser != null && entity.getParticipants().stream()
                                .anyMatch(p -> Long.valueOf(p.getUser().getId()).equals(currentUser.getId()));

                Map<String, Object> data = new HashMap<>();
                data.put("post", post);
                data.put("messages", messages);
                data.put("participants", entity.getParticipants());
                data.put("alreadyJoined", alreadyJoined);
                data.put("currentUserId", currentUser != null ? currentUser.getId() : null);

                return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                                .status(200).message("Thành công").data(data).build());
        }

        @PostMapping
        public ResponseEntity<ApiResponse<MatchPostResponseDTO>> createPost(
                        @RequestBody MatchPost post) {

                User currentUser = authenticationFacade.getCurrentUser();
                if (currentUser == null)
                        throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");

                MatchPostResponseDTO created = matchPostService.createPost(post, currentUser);
                return ResponseEntity.ok(ApiResponse.<MatchPostResponseDTO>builder()
                                .status(200).message("Bài đăng đã được tạo thành công").data(created).build());
        }

        @PostMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<String>> cancelPost(@PathVariable Long id) {
                User currentUser = authenticationFacade.getCurrentUser();
                if (currentUser == null)
                        throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
                matchPostService.cancelPost(id, currentUser);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200).message("Bài đăng đã được huỷ thành công").data(null).build());
        }

        @PostMapping("/{id}/join")
        public ResponseEntity<ApiResponse<String>> joinPost(@PathVariable Long id) {
                User currentUser = authenticationFacade.getCurrentUser();
                if (currentUser == null)
                        throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
                matchPostService.joinPost(id, currentUser);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200).message("Bạn đã tham gia thành công").data(null).build());
        }

        @PostMapping("/{id}/leave")
        public ResponseEntity<ApiResponse<String>> leavePost(@PathVariable Long id) {
                User currentUser = authenticationFacade.getCurrentUser();
                if (currentUser == null)
                        throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
                matchPostService.leavePost(id, currentUser);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200).message("Bạn đã rời bài đăng thành công").data(null).build());
        }

        @PostMapping("/{postId}/kick/{userId}")
        public ResponseEntity<ApiResponse<String>> kickParticipant(
                        @PathVariable Long postId, @PathVariable Long userId) {
                User currentUser = authenticationFacade.getCurrentUser();
                if (currentUser == null)
                        throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
                matchPostService.kickParticipant(postId, userId, currentUser);
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .status(200).message("Đã loại người tham gia khỏi trận đấu").data(null).build());
        }
}
