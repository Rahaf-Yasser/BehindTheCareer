package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
public class InterviewSessionResponse {
    private Integer sessionId;
    private String selectedField;
    private Integer durationMinutes;
    private String status;
    private List<InterviewQuestionResponse> questions;

    // Overall session analysis
    private String overallFeedback;
    private String strongAreas;
    private String weakAreas;
    private String studyRecommendations;
    private String overallVerdict; // Strong / Average / Needs Improvement

    // Scores
    private Double accuracyScore;

    private String createdAt; // Changed from LocalDateTime to String
    private String completedAt; // Changed from LocalDateTime to String

    // Helper method to convert LocalDateTime to String when building response
    public static InterviewSessionResponseBuilder builderWithDates() {
        return builder();
    }
}