package com.example.quanly.repository;

import com.example.quanly.domain.MatchPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM MatchPost p WHERE p.id = :id")
    Optional<MatchPost> findByIdForUpdate(@Param("id") Long id);

    // Khi không có skillLevel
    Page<MatchPost> findByAreaAndPlayDateAndStatus(String area, LocalDate playDate, String status, Pageable pageable);

    // Khi có skillLevel
    Page<MatchPost> findByAreaAndPlayDateAndSkillLevelAndStatus(
            String area, LocalDate playDate, String skillLevel, String status, Pageable pageable);

    List<MatchPost> findByUserId(Long userId);
    Page<MatchPost> findAll(Pageable pageable);

    Page<MatchPost> findByStatusNot(String status, Pageable pageable);

    /**
     * Bulk-update: flip mọi MatchPost có status ∈ statuses và playDate < today thành "expired".
     * Tránh load toàn bộ bảng vào memory như findAll() từng làm.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MatchPost p SET p.status = 'expired' " +
           "WHERE p.status IN :statuses AND p.playDate IS NOT NULL AND p.playDate < :today")
    int updateExpiredPosts(@Param("statuses") Collection<String> statuses,
                           @Param("today") LocalDate today);
}
