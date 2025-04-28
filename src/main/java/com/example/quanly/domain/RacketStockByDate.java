package com.example.quanly.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class RacketStockByDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Mã ID của cây vợt
    private Long racketId;
    private LocalDate date;
    private int availableStock; // số lượng vợt có sẵn
    private int reservedStock; // private int reservedQuantity; // số lượng đã đặt trước
    private int rentalStock; // soo vot luong da cho thue
    private int totalStock; // số lượng vợt trong kho
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
