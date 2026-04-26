package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileFieldsResponse {
    private List<String> availableFields; // Fields from the user's profile
    private List<Integer> availableDurations; // Always [10, 15, 20]
}