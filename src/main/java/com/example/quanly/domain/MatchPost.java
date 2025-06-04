package com.example.quanly.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "match_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "play_date", nullable = false)
    private LocalDate playDate;

    @Column(nullable = false)
    private String area;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(name = "skill_level")
    private String skillLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status = "open";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "max_participants")
    private int maxParticipants;

    @Column(name = "current_participants")
    private int currentParticipants;

    @OneToMany(mappedBy = "matchPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchParticipant> participants;

    @OneToMany(mappedBy = "matchPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    @Transient
    public String getPlayDateStr() {
        return playDate != null ? playDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }
}
