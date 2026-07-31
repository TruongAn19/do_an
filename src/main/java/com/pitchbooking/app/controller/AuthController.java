package com.pitchbooking.app.controller;

import com.pitchbooking.app.config.JwtTokenProvider;
import com.pitchbooking.app.domain.User;
import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.domain.dto.JwtAuthResponse;
import com.pitchbooking.app.domain.dto.LoginRequest;
import com.pitchbooking.app.domain.dto.RegisterDTO;
import com.pitchbooking.app.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Name of the httpOnly cookie carrying the JWT. Must match {@link com.pitchbooking.app.config.JwtAuthenticationFilter}. */
    public static final String AUTH_COOKIE_NAME = "accessToken";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /** JWT TTL in milliseconds — used to set the cookie Max-Age. */
    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMillis;

    /** Set Secure flag on the auth cookie. Disable for plain-http localhost dev. */
    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;

    /** Optional cookie domain (e.g. ".example.com") for cross-subdomain deployments. */
    @Value("${auth.cookie.domain:}")
    private String cookieDomain;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.generateToken(loginRequest.getEmail());

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            // FIX-S1: token goes into an httpOnly cookie so JS can't read it (XSS-hardened).
            // Body returns null accessToken — the FE no longer needs to know it.
            JwtAuthResponse authResponse = new JwtAuthResponse(null, loginRequest.getEmail(), role);

            String setCookieHeader = buildAuthCookie(jwt, Duration.ofMillis(jwtExpirationMillis)).toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, setCookieHeader)
                    .body(ApiResponse.<JwtAuthResponse>builder()
                            .status(200)
                            .message("Đăng nhập thành công")
                            .data(authResponse)
                            .build());

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<JwtAuthResponse>builder()
                    .status(401)
                    .message("Email hoặc mật khẩu không đúng")
                    .data(null)
                    .build());
        }
    }

    /**
     * Clears the httpOnly auth cookie. Idempotent — safe to call when not logged in.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        String clearCookieHeader = buildAuthCookie("", Duration.ZERO).toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookieHeader)
                .body(ApiResponse.<Void>builder()
                        .status(200)
                        .message("Đăng xuất thành công")
                        .data(null)
                        .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        if (userService.findByEmail(registerDTO.getEmail()) != null) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status(400)
                    .message("Email đã được sử dụng")
                    .data(null)
                    .build());
        }
        User user = userService.registerDTOtoUser(registerDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(userService.getRoleByName("USER"));
        userService.handleSaveUser(user);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Đăng ký thành công")
                .data(null)
                .build());
    }

    private ResponseCookie buildAuthCookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(AUTH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }
}
