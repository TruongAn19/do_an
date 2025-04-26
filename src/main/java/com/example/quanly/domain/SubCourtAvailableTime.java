package com.example.quanly.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



@Entity
@Table(name = "subcourt_available_time")
public class SubCourtAvailableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sub_court_id")
    private SubCourt subCourt;

    @ManyToOne
    @JoinColumn(name = "available_time_id")
    private AvailableTime availableTime;

    // @Column(nullable = false)
    // private boolean isBooked = false; // Mặc định: chưa đặt

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubCourt getSubCourt() {
        return subCourt;
    }

    public void setSubCourt(SubCourt subCourt) {
        this.subCourt = subCourt;
    }

    public AvailableTime getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(AvailableTime availableTime) {
        this.availableTime = availableTime;
    }

    

    
}
