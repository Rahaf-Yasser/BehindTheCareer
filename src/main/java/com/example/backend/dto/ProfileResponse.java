package com.example.backend.dto;

import com.example.backend.model.ExperienceLevel;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Integer profileId;
    private Integer userId;
    private String fullName;
    private String email;
    private String bio;
    private List<String> fields;
    private List<String> skills;
    private ExperienceLevel experienceLevel;
    private String profilePicture;
    private String updatedAt;

    public void setUpdatedAt(LocalDateTime dateTime) {
        if (dateTime != null) {
            this.updatedAt = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        }
    }
}