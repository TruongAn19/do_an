package com.example.quanly.service;

import com.example.quanly.domain.User;
import com.example.quanly.repository.IAuthenticationFacade;
import com.example.quanly.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;


@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    private final HttpServletRequest request;
    private final UserRepository userRepository;

    public AuthenticationFacade(HttpServletRequest request, UserRepository userRepository) {
        this.request = request;
        this.userRepository = userRepository;
    }

    @Override
    public User getCurrentUser() {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userIdObj = session.getAttribute("id");
        if (userIdObj == null) {
            return null;
        }
        Long userId;
        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else {
            // Nếu lưu dưới dạng String hoặc kiểu khác thì chuyển đổi tại đây
            try {
                userId = Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // Tìm user trong DB
        return userRepository.findById(userId).orElse(null);
    }
}

