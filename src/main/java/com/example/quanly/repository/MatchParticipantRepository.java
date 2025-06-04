package com.example.quanly.repository;

import com.example.quanly.domain.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    boolean existsByMatchPostIdAndUserId(Long matchPostId, Long userId);
    List<MatchParticipant> findByMatchPostId(Long matchPostId);
    int countByMatchPostId(Long matchPostId);

    // Tìm participant bằng postId và userId
    Optional<MatchParticipant> findByMatchPostIdAndUserId(Long matchPostId, Long userId);
}
