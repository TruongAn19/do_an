package com.example.quanly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_match_participant_post_user",
                columnNames = {"match_post_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_post_id", nullable = false)
    @JsonIgnoreProperties({"participants", "messages", "user"})
    private MatchPost matchPost;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"matchPosts", "participations", "messages", "products", "password"})
    private User user;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
}
