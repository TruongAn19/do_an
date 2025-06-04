package com.example.quanly.service;

import com.example.quanly.domain.MatchParticipant;
import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.User;
import com.example.quanly.repository.MatchParticipantRepository;
import com.example.quanly.repository.MatchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MatchPostService {
    private final MatchPostRepository matchPostRepository;
    private final MatchParticipantRepository participantRepository;
    private final NotificationService notificationService;

    public List<MatchPost> searchPosts(String area, LocalDate playDate) {
        return matchPostRepository.findByAreaAndPlayDateAndStatus(area, playDate, "open");
    }

    public MatchPost createPost(MatchPost post, User creator) {
        post.setUser(creator);
        post.setStatus("open");
        post.setCurrentParticipants(0); // Khởi tạo số người tham gia ban đầu

        if (post.getMaxParticipants() <= 0) {
            throw new RuntimeException("Số người tham gia tối đa phải lớn hơn 0");
        }

        return matchPostRepository.save(post);
    }

    public MatchPost getPostById(Long id) {
        return matchPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public MatchParticipant joinPost(Long postId, User user) {
        MatchPost post = getPostById(postId);

        // Kiểm tra trạng thái bài đăng
        if ("closed".equals(post.getStatus())) {
            throw new RuntimeException("Bài đăng đã bị huỷ");
        }

        if ("full".equals(post.getStatus())) {
            throw new RuntimeException("Bài đăng đã đủ người tham gia");
        }

        if (participantRepository.existsByMatchPostIdAndUserId(postId, user.getId())) {
            throw new RuntimeException("Bạn đã tham gia bài đăng này rồi");
        }

        MatchParticipant participant = new MatchParticipant();
        participant.setMatchPost(post);
        participant.setUser(user);

        MatchParticipant savedParticipant = participantRepository.save(participant);

        // Cập nhật số người tham gia
        int currentParticipants = participantRepository.countByMatchPostId(postId);
        post.setCurrentParticipants(currentParticipants);

        // Kiểm tra nếu đã đủ người thì đổi status
        if (currentParticipants >= post.getMaxParticipants()) {
            post.setStatus("full");
        }

        matchPostRepository.save(post);

        // Gửi thông báo cho chủ bài đăng
        notificationService.notifyNewParticipant(post.getUser(), user, post);

        return savedParticipant;
    }

    public void cancelPost(Long postId, User requester) {
        MatchPost post = getPostById(postId);

        // Chỉ chủ bài đăng mới được huỷ
        if (!Objects.equals(post.getUser().getId(), requester.getId())) {
            throw new RuntimeException("Bạn không có quyền huỷ bài đăng này");
        }

        post.setStatus("closed");
        matchPostRepository.save(post);

        // Gửi thông báo cho tất cả người tham gia
        List<MatchParticipant> participants = participantRepository.findByMatchPostId(postId);
        for (MatchParticipant participant : participants) {
            notificationService.notifyPostCancelled(participant.getUser(), post);
        }
    }

    public void leavePost(Long postId, User user) {
        MatchPost post = getPostById(postId);

        // Kiểm tra người dùng có tham gia bài đăng không
        MatchParticipant participant = participantRepository.findByMatchPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa tham gia bài đăng này"));

        participantRepository.delete(participant);

        // Cập nhật lại số người tham gia
        int currentParticipants = participantRepository.countByMatchPostId(postId);
        post.setCurrentParticipants(currentParticipants);

        // Nếu trước đó là full mà bây giờ có người rời thì chuyển lại thành open
        if ("full".equals(post.getStatus()) && currentParticipants < post.getMaxParticipants()) {
            post.setStatus("open");
        }

        matchPostRepository.save(post);

        // Gửi thông báo cho chủ bài đăng
        notificationService.notifyParticipantLeft(post.getUser(), user, post);
    }
}
