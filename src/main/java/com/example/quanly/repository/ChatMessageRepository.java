package com.example.quanly.repository;

import com.example.quanly.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByMatchPostIdOrderBySentAtAsc(Long matchPostId);
}
