package com.example.quanly.scheduler;

import com.example.quanly.service.MatchPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MatchPostScheduler {

    private final MatchPostService matchPostService;

    @Scheduled(cron = "0 * * * * ?")  // Tạm thời test mỗi phút
    public void updateExpiredPostsTask() {
        System.out.println("⏰ Running scheduled task at " + LocalDateTime.now());
        matchPostService.updateExpiredPostsDaily(); // Gọi method từ service
    }
}
