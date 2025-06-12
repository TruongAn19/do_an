package com.example.quanly.scheduler;

import com.example.quanly.service.MatchPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchPostScheduler {

    private final MatchPostService matchPostService;

    @Scheduled(cron = "0 0 23 * * ?")  // Tạm thời test mỗi phút
    public void updateExpiredPostsTask() {
        matchPostService.updateExpiredPostsDaily(); // Gọi method từ service
    }
}
