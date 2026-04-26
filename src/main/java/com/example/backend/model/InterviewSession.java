package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interview_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private String selectedField;

    @Column(nullable = false)
    private Integer durationMinutes;

    // PENDING → IN_PROGRESS → COMPLETED
    @Column(nullable = false)
    private String status;

    // Overall feedback paragraph
    @Column(columnDefinition = "TEXT")
    private String overallFeedback;

    // Strong and weak areas summary
    @Column(columnDefinition = "TEXT")
    private String strongAreas;

    @Column(columnDefinition = "TEXT")
    private String weakAreas;

    // What to study next
    @Column(columnDefinition = "TEXT")
    private String studyRecommendations;

    // Strong / Average / Needs Improvement
    private String overallVerdict;

    // Scores out of 10
    private Double accuracyScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterviewQuestion> questions;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = "IN_PROGRESS";
    }
}