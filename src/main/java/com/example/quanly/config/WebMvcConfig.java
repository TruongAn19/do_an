package com.example.quanly.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${app.upload.directory:uploads/images}")
    private String uploadDirectory;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chỉ áp dụng Rate Limit cho các endpoint quan trọng như Hold và Place Booking
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/client/bookings/hold")
                .addPathPatterns("/api/v1/client/bookings/place");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDirectory).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/resources/images/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
