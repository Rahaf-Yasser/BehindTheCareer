package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterviewQuestionResponse {
    private Integer questionId;
    private Integer questionOrder;
    private String questionText;
    private String userAnswer;

    // Technical analysis fields
    private String aiFeedback;
    private String suggestedImprovements;
    private String correctness; // Good / Partial / Poor
    private String completeness; // Good / Partial / Poor
    private String relevance; // Good / Partial / Poor
    private String depth; // Good / Partial / Poor
    private Double score;
    private Boolean answered;
}