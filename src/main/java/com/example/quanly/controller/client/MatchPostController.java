package com.example.quanly.controller.client;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ChatMessageDto;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// MatchPostController.java
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/match-posts")
public class MatchPostController {
    private final MatchPostService matchPostService;
    private final ChatService chatService;
    private final AuthenticationFacade authenticationFacade;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchParticipantRepository matchParticipantRepository;

    @GetMapping
    public String searchPosts(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate playDate,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<MatchPost> posts;
        User currentUser = authenticationFacade.getCurrentUser();

        if (area != null && playDate != null) {
            posts = matchPostService.searchPosts(area, playDate, skillLevel, pageable);
        } else {
            posts = matchPostService.getAllPosts(pageable);
        }

        Set<Long> joinedPostIds = currentUser != null
                ? matchParticipantRepository.findPostIdsByUserId(currentUser.getId())
                : Collections.emptySet();

        // Tạo list Map chứa dữ liệu và chuỗi ngày đã format
        List<Map<String, Object>> postsView = posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("area", post.getArea());
            map.put("status", post.getStatus());
            map.put("timeSlot", post.getTimeSlot());
            map.put("skillLevel", post.getSkillLevel());
            map.put("description", post.getDescription());
            map.put("maxParticipants", post.getMaxParticipants());
            map.put("currentParticipants", post.getCurrentParticipants());
            // Format LocalDate thành String dd/MM/yyyy
            map.put("playDateStr", post.getPlayDate() != null
                    ? post.getPlayDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "");
            map.put("owner", currentUser != null
                    && post.getUser() != null
                    && Objects.equals(
                    currentUser.getId(),
                    post.getUser().getId()
            ));
            map.put("joined", currentUser != null && joinedPostIds.contains(post.getId()));
            return map;

        }).collect(Collectors.toList());

        model.addAttribute("posts", postsView);
        model.addAttribute("searchArea", area);
        model.addAttribute("searchSkillLevel", skillLevel);
        model.addAttribute("searchDate", playDate != null ? playDate.toString() : "");
        model.addAttribute("page", posts);

        return "client/match-post/list";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        MatchPost post = matchPostService.getPostById(id);

        User currentUser = authenticationFacade.getCurrentUser();

        // Format các ChatMessage thành DTO
        List<ChatMessageDto> messages = chatService.getMessageDtos(post.getId());
        boolean alreadyJoined = post.getParticipants().stream()
                .anyMatch(participant -> Long.valueOf(participant.getUser().getId()).equals(currentUser.getId()));

        model.addAttribute("post", post);
        model.addAttribute("messages", messages); // dùng DTO
        model.addAttribute("playDate", post.getPlayDate().toString());
        model.addAttribute("newMessage", new ChatMessageDto()); // tạo rỗng để binding form nếu cần
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("participants", post.getParticipants());
        model.addAttribute("alreadyJoined", alreadyJoined);
        return "client/match-post/detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("matchPost", new MatchPost());
        return "client/match-post/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute MatchPost post,
                             @RequestParam int maxParticipants,
                             RedirectAttributes redirectAttributes) {
        User currentUser = authenticationFacade.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
        }

        post.setMaxParticipants(maxParticipants);
        MatchPost createdPost = matchPostService.createPost(post, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bài đăng đã được tạo thành công");
        return "redirect:/match-posts/" + createdPost.getId();
    }

    @PostMapping("/{id}/cancel")
    public String cancelPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = authenticationFacade.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
        }

        matchPostService.cancelPost(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bài đăng đã được huỷ thành công");
        return "redirect:/match-posts";
    }

    @PostMapping("/{id}/join")
    public String joinPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = authenticationFacade.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
        }

        matchPostService.joinPost(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bạn đã tham gia thành công");
        return "redirect:/match-posts/" + id;
    }

    @PostMapping("/{id}/leave")
    public String leavePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = authenticationFacade.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
        }

        matchPostService.leavePost(id, currentUser);
        redirectAttributes.addFlashAttribute("success", "Bạn đã rời bài đăng thành công");
        return "redirect:/match-posts/" + id;
    }

    @PostMapping("/{postId}/kick/{userId}")
    public String kickParticipant(@PathVariable Long postId,
                                  @PathVariable Long userId,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = authenticationFacade.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Chưa đăng nhập hoặc session hết hạn");
        }

        try {
            matchPostService.kickParticipant(postId, userId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đã loại người tham gia khỏi trận đấu");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/match-posts/" + postId;
    }


}
