package com.example.quanly.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chỉ áp dụng Rate Limit cho các endpoint quan trọng như Hold và Place Booking
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/client/bookings/hold")
                .addPathPatterns("/api/v1/client/bookings/place");
    }
}
