package com.example.quanly.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "temporary_booking", uniqueConstraints = @UniqueConstraint(
        name = "uk_temporary_booking_slot",
        columnNames = {"sub_court_id", "available_time_id", "booking_date"}))
public class TemporaryBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "sub_court_id")
    private SubCourt subCourt;

    @ManyToOne
    @JoinColumn(name = "available_time_id")
    private AvailableTime availableTime;

    private LocalDate bookingDate;

    private LocalDateTime holdStartTime;
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }

}
