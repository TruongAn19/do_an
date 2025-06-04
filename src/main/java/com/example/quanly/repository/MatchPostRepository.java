package com.example.quanly.repository;

import com.example.quanly.domain.MatchPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchPostRepository extends JpaRepository<MatchPost, Long> {
    List<MatchPost> findByAreaAndPlayDateAndStatus(String area, LocalDate playDate, String status);
    List<MatchPost> findByUserId(Long userId);
}