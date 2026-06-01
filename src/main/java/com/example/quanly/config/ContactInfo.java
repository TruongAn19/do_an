package com.example.quanly.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Thông tin liên hệ hiển thị cho user khi cần hỏi về hoàn cọc.
 * Đọc từ application.properties (không hardcode trong service/FE).
 */
@Getter
@Component
public class ContactInfo {

    @Value("${app.contact.hotline:0123456789}")
    private String hotline;

    @Value("${app.contact.email:admin@badmintonhub.vn}")
    private String email;
}
