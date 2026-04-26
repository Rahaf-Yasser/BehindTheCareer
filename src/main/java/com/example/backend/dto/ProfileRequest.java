package com.example.backend.dto;

import com.example.backend.model.ExperienceLevel;
import lombok.Data;
import java.util.List;

@Data
public class ProfileRequest {
    private String bio;
    private List<String> fields; // e.g. ["Software Developer", "Software Tester"]
    private List<String> skills; // e.g. ["Java", "React", "MySQL"]
    private ExperienceLevel experienceLevel;
    private String profilePicture;
}