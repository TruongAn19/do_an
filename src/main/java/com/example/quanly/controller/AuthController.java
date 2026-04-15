package com.example.quanly.controller;

import com.example.quanly.config.JwtTokenProvider;
import com.example.quanly.domain.User;
import com.example.quanly.domain.dto.ApiResponse;
import com.example.quanly.domain.dto.JwtAuthResponse;
import com.example.quanly.domain.dto.LoginRequest;
import com.example.quanly.domain.dto.RegisterDTO;
import com.example.quanly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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

            JwtAuthResponse authResponse = new JwtAuthResponse(jwt, loginRequest.getEmail(), role);

            return ResponseEntity.ok(ApiResponse.<JwtAuthResponse>builder()
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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterDTO registerDTO) {
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

}
