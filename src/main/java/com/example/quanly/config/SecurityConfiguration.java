package com.example.quanly.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
        private String allowedOrigins;

        @Bean
        public DaoAuthenticationProvider authProvider(PasswordEncoder passwordEncoder,
                        UserDetailsService userDetailsService) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);
                provider.setHideUserNotFoundExceptions(false);
                return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                // Trim khoảng trắng thừa khi split
                List<String> origins = Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .toList();
                config.setAllowedOrigins(origins);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Authorization"));
                config.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        /**
         * Đăng ký CorsFilter với mức ưu tiên cao nhất để xử lý preflight OPTIONS
         * request TRƯỚC KHI Spring Security filter chain chạy.
         */
        // @Bean
        // public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        // FilterRegistrationBean<CorsFilter> bean =
        // new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        // bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // return bean;
        // }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(sess -> sess
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // Public endpoints — không cần Token
                                                .requestMatchers(
                                                                "/api/v1/auth/**",
                                                                "/api/v1/products/**",
                                                                "/api/v1/equipments/**",
                                                                "/api/v1/client/home",
                                                                "/api/v1/equipment-stock/**",
                                                                "/api/v1/ntfy-sse/**",
                                                                "/api/v1/payments/vnpay-callback",
                                                "/api/v1/mock-payment/**",
                                                                "/api/v1/rentals/*/equipments",
                                                                "/api/v1/ai/**",
                                                                "/ws/**")

                                                .permitAll()
                                                // Match-post cần đăng nhập
                                                .requestMatchers("/api/v1/match-posts/**").authenticated()
                                                // Staff xem booking & rental
                                                .requestMatchers(
                                                                "/api/v1/admin/bookings/**",
                                                                "/api/v1/admin/rentals/**")
                                                .hasAnyRole("STAFF", "ADMIN")
                                                // Toàn bộ admin còn lại chỉ ADMIN
                                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                                // Mọi request khác phải xác thực
                                                .anyRequest().authenticated())
                                // Gắn JWT filter vào trước UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                // Trả JSON thay vì redirect trang lỗi
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler((req, res, e) -> {
                                                        res.setStatus(403);
                                                        res.setContentType("application/json;charset=UTF-8");
                                                        res.getWriter().write(
                                                                        "{\"status\":403,\"message\":\"Không có quyền truy cập\",\"data\":null}");
                                                })
                                                .authenticationEntryPoint((req, res, e) -> {
                                                        res.setStatus(401);
                                                        res.setContentType("application/json;charset=UTF-8");
                                                        res.getWriter().write(
                                                                        "{\"status\":401,\"message\":\"Chưa xác thực. Vui lòng đăng nhập.\",\"data\":null}");
                                                }));

                return http.build();
        }
}
