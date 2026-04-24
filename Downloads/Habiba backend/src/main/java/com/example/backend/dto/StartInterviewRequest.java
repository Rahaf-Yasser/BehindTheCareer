package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartInterviewRequest {

    // Must match one of the fields in the user's profile (e.g. "Software
    // Developer")
    @NotBlank(message = "Selected field is required")
    private String selectedField;

    // Must be 10, 15, or 20
    @NotNull(message = "Duration is required")
    private Integer durationMinutes;
}