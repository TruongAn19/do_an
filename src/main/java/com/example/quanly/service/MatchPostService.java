package com.example.quanly.service;

import com.example.quanly.domain.MatchParticipant;
import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.repository.MatchParticipantRepository;
import com.example.quanly.repository.MatchPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MatchPostService {
    private final MatchPostRepository matchPostRepository;
    private final MatchParticipantRepository participantRepository;
    private final NotificationService notificationService;

    public Page<MatchPost> getAllPosts(Pageable pageable) {
        return matchPostRepository.findByStatusNot("expired", pageable);
    }

    public Page<MatchPost> searchPosts(String area, LocalDate playDate, String skillLevel, Pageable pageable) {
        if (skillLevel == null || skillLevel.isBlank()) {
            return matchPostRepository.findByAreaAndPlayDateAndStatus(area, playDate, "open", pageable);
        } else {
            return matchPostRepository.findByAreaAndPlayDateAndSkillLevelAndStatus(area, playDate, skillLevel, "open", pageable);
        }
    }

    public MatchPost createPost(MatchPost post, User creator) {
        if (post.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Số người tham gia tối đa phải lớn hơn 0");
        }

        post.setUser(creator);
        post.setStatus("open");
        post.setCurrentParticipants(0);
        return matchPostRepository.save(post);
    }

    public MatchPost getPostById(Long id) {
        return matchPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));
    }

    public MatchParticipant joinPost(Long postId, User user) {
        MatchPost post = getPostById(postId);

        validateJoinConditions(post, user);

        MatchParticipant participant = new MatchParticipant();
        participant.setMatchPost(post);
        participant.setUser(user);

        MatchParticipant saved = participantRepository.save(participant);
        updateParticipantCount(post);

        notificationService.notifyNewParticipant(post.getUser(), user, post);

        return saved;
    }

    public void leavePost(Long postId, User user) {
        MatchPost post = getPostById(postId);

        MatchParticipant participant = participantRepository.findByMatchPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tham gia bài đăng này"));

        participantRepository.delete(participant);
        updateParticipantCount(post);

        notificationService.notifyParticipantLeft(post.getUser(), user, post);
    }

    public void cancelPost(Long postId, User requester) {
        MatchPost post = getPostById(postId);

        if (!Objects.equals(post.getUser().getId(), requester.getId())) {
            throw new RuntimeException("Bạn không có quyền huỷ bài đăng này");
        }

        post.setStatus("closed");
        matchPostRepository.save(post);

        List<MatchParticipant> participants = participantRepository.findByMatchPostId(postId);
        for (MatchParticipant p : participants) {
            notificationService.notifyPostCancelled(p.getUser(), post);
        }
    }

    public void kickParticipant(Long postId, Long userIdToKick, User requester) {
        MatchPost post = getPostById(postId);

        if (!Objects.equals(post.getUser().getId(), requester.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền kick người này");
        }

        MatchParticipant participant = participantRepository.findByMatchPostIdAndUserId(postId, userIdToKick)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tham gia bài đăng"));

        participantRepository.delete(participant);
        updateParticipantCount(post);

        notificationService.notifyUserKicked(participant.getUser(), post);
    }

    private void updateParticipantCount(MatchPost post) {
        int current = participantRepository.countByMatchPostId(post.getId());
        post.setCurrentParticipants(current);

        if ("full".equals(post.getStatus()) && current < post.getMaxParticipants()) {
            post.setStatus("open");
        } else if ("open".equals(post.getStatus()) && current >= post.getMaxParticipants()) {
            post.setStatus("full");
        }

        matchPostRepository.save(post);
    }

    private void validateJoinConditions(MatchPost post, User user) {
        if ("closed".equals(post.getStatus())) {
            throw new RuntimeException("Bài đăng đã bị huỷ");
        }
        if ("full".equals(post.getStatus())) {
            throw new RuntimeException("Bài đăng đã đủ người tham gia");
        }
        if (participantRepository.existsByMatchPostIdAndUserId(post.getId(), user.getId())) {
            throw new RuntimeException("Bạn đã tham gia bài đăng này rồi");
        }
    }

    @Transactional
    public void updateExpiredPostsDaily() {
        LocalDate today = LocalDate.now();
        List<MatchPost> posts = matchPostRepository.findAll();

        for (MatchPost post : posts) {
            if (("closed".equals(post.getStatus()) || "open".equals(post.getStatus()))
                    && post.getPlayDate() != null
                    && post.getPlayDate().isBefore(today)) {
                post.setStatus("expired");
                matchPostRepository.save(post);
            }
        }
        System.out.println("Updated expired posts at " + LocalDateTime.now());
    }
}
