package com.example.quanly.domain;

import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime time; // Thời gian (ví dụ: 2:00, 3:00, 4:00)

    @ManyToMany(mappedBy = "availableTimes")
    private Set<Product> products;

    @OneToMany(mappedBy = "availableTime")
    private Set<Booking> orders;

    @OneToMany(mappedBy = "availableTime")
    private Set<BookingDetail> orderDetails;
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AvailableTime that = (AvailableTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
