package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_resume_analyzer")
public class AIResumeAnalyzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer analysisId;

    private Integer resumeId;
    private Integer chatGPTInteractionId;

    @Column(columnDefinition = "TEXT")
    private String feedbackText;

    @Column(columnDefinition = "TEXT")
    private String improvementSuggestions;

    private LocalDateTime analyzedAt;

    public AIResumeAnalyzer() {
        this.analyzedAt = LocalDateTime.now();
    }

    public Integer getAnalysisId() { return analysisId; }
    public void setAnalysisId(Integer analysisId) { this.analysisId = analysisId; }

    public Integer getResumeId() { return resumeId; }
    public void setResumeId(Integer resumeId) { this.resumeId = resumeId; }

    public Integer getChatGPTInteractionId() { return chatGPTInteractionId; }
    public void setChatGPTInteractionId(Integer chatGPTInteractionId) { this.chatGPTInteractionId = chatGPTInteractionId; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public String getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(String improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}