package com.example.quanly.service;

import com.example.quanly.domain.BookingStatus;
import com.example.quanly.domain.User;
import com.example.quanly.exception.ResourceNotFoundException;
import com.example.quanly.repository.BookingRepository;
import com.example.quanly.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLevelService {

    private static final String NORMAL = "NORMAL";
    private static final String SILVER = "SILVER";
    private static final String GOLD = "GOLD";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Value("${membership.silver.min-spend:2000000}")
    private double silverMinSpend;

    @Value("${membership.gold.min-spend:5000000}")
    private double goldMinSpend;

    @Transactional
    public String evaluateAndUpgrade(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng id=" + userId));

        double paidTotal = bookingRepository.sumTotalPriceByUserIdAndStatus(
                userId, BookingStatus.DA_THANH_TOAN);
        String earnedLevel = paidTotal >= goldMinSpend
                ? GOLD
                : paidTotal >= silverMinSpend ? SILVER : NORMAL;

        String currentLevel = normalize(user.getMemberLevel());
        if (rank(earnedLevel) > rank(currentLevel)) {
            user.setMemberLevel(earnedLevel);
            userRepository.save(user);
            return earnedLevel;
        }
        return currentLevel;
    }

    private String normalize(String level) {
        if (SILVER.equalsIgnoreCase(level)) return SILVER;
        if (GOLD.equalsIgnoreCase(level)) return GOLD;
        return NORMAL;
    }

    private int rank(String level) {
        return switch (level) {
            case GOLD -> 2;
            case SILVER -> 1;
            default -> 0;
        };
    }
}
