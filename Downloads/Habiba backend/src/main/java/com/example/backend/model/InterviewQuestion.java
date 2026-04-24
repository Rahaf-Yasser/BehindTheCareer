package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", nullable = false)
    private InterviewSession session;

    private Integer questionOrder;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // Transcribed from audio via Whisper
    @Column(columnDefinition = "TEXT")
    private String userAnswer;

    // Overall AI feedback paragraph
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    // Specific improvement suggestions
    @Column(columnDefinition = "TEXT")
    private String suggestedImprovements;

    // Dimension ratings: Good / Partial / Poor
    private String correctness;
    private String completeness;
    private String relevance;
    private String depth;

    // Score out of 10
    private Double score;

    @Column(nullable = false)
    @Builder.Default
    private Boolean answered = false;
}