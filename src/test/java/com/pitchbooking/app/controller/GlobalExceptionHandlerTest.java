package com.pitchbooking.app.controller;

import com.pitchbooking.app.domain.dto.ApiResponse;
import com.pitchbooking.app.exception.BusinessConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessConflict_returns409WithStableErrorCode() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/admin/rentals/10/status");

        ResponseEntity<ApiResponse<Void>> response = handler.handleConflict(
                new BusinessConflictException("Trạng thái không hợp lệ"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BUSINESS_CONFLICT");
        assertThat(response.getBody().getMessage()).isEqualTo("Trạng thái không hợp lệ");
    }

    @Test
    void internalIllegalState_remains500() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/rentals/10");

        ResponseEntity<?> response = handler.handleRuntimeException(
                new IllegalStateException("Kho dữ liệu không nhất quán"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_ERROR");
    }
}
