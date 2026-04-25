package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Integer questionId;

    // Text transcribed from the user's audio via OpenAI Whisper
    @NotBlank(message = "Answer text is required")
    private String answerText;
}