package com.example.quanly.repository;

import com.example.quanly.domain.MatchPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long> {
    // Khi không có skillLevel
    Page<MatchPost> findByAreaAndPlayDateAndStatus(String area, LocalDate playDate, String status, Pageable pageable);

    // Khi có skillLevel
    Page<MatchPost> findByAreaAndPlayDateAndSkillLevelAndStatus(
            String area, LocalDate playDate, String skillLevel, String status, Pageable pageable);

    List<MatchPost> findByUserId(Long userId);
    Page<MatchPost> findAll(Pageable pageable);

    Page<MatchPost> findByStatusNot(String status, Pageable pageable);
}