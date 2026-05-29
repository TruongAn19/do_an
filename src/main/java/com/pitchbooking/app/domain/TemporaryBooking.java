package com.pitchbooking.app.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "temporary_booking")
public class TemporaryBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "sub_pitch_id")
    private SubPitch subPitch;

    @ManyToOne
    @JoinColumn(name = "available_time_id")
    private AvailableTime availableTime;

    private LocalDate bookingDate;

    private LocalDateTime holdExpiresAt;

    public boolean isExpired() {
        return holdExpiresAt.isBefore(LocalDateTime.now());
    }

}
