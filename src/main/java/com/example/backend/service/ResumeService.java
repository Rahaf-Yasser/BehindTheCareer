package com.example.backend.service;

import com.example.backend.dto.ResumeDTO;
import com.example.backend.model.Resume;
import com.example.backend.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────
    // GET all resumes for a user
    // ─────────────────────────────────────────────
    public List<ResumeDTO> getResumesByUser(Integer userId) {
        return resumeRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // GET single resume by ID
    // ─────────────────────────────────────────────
    public ResumeDTO getResumeById(Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));
        return toDTO(resume);
    }

    // ─────────────────────────────────────────────
    // CREATE a new resume
    // ─────────────────────────────────────────────
    public ResumeDTO createResume(ResumeDTO dto) {
        Resume resume = new Resume();
        resume.setUserId(dto.getUserId());
        resume.setProfileId(dto.getProfileId());
        resume.setTitle(dto.getTitle() != null ? dto.getTitle() : "My Resume");
        resume.setTemplate(dto.getTemplate() != null ? dto.getTemplate() : "classic");
        resume.setStatus("Draft");
        resume.setContent(dto.getContent() != null ? dto.getContent() : buildEmptyContent());
        resume.setCreatedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);
        return toDTO(saved);
    }

    // ─────────────────────────────────────────────
    // AUTO-SAVE / UPDATE resume content
    // Called every time user types — only updates content + status
    // ─────────────────────────────────────────────
    public ResumeDTO autoSave(Integer resumeId, ResumeDTO dto) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));

        if (dto.getContent() != null) {
            resume.setContent(dto.getContent());
        }
        if (dto.getTitle() != null) {
            resume.setTitle(dto.getTitle());
        }
        if (dto.getTemplate() != null) {
            resume.setTemplate(dto.getTemplate());
        }
        if (dto.getStatus() != null) {
            resume.setStatus(dto.getStatus());
        }

        Resume saved = resumeRepository.save(resume);
        return toDTO(saved);
    }

    // ─────────────────────────────────────────────
    // DELETE resume
    // ─────────────────────────────────────────────
    public void deleteResume(Integer resumeId) {
        if (!resumeRepository.existsById(resumeId)) {
            throw new RuntimeException("Resume not found with id: " + resumeId);
        }
        resumeRepository.deleteById(resumeId);
    }

    // ─────────────────────────────────────────────
    // VALIDATE resume before export
    // Returns list of missing required fields
    // ─────────────────────────────────────────────
    public List<String> validateForExport(Integer resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));

        List<String> missingFields = new java.util.ArrayList<>();
        JsonNode content = resume.getContent();

        if (content == null) {
            missingFields.add("Resume content is empty");
            return missingFields;
        }

        JsonNode personalInfo = content.get("personalInfo");
        if (personalInfo == null || isBlank(personalInfo, "fullName")) {
            missingFields.add("Full Name");
        }
        if (personalInfo == null || isBlank(personalInfo, "email")) {
            missingFields.add("Email");
        }
        if (personalInfo == null || isBlank(personalInfo, "phone")) {
            missingFields.add("Phone");
        }

        JsonNode experience = content.get("experience");
        if (experience == null || !experience.isArray() || experience.isEmpty()) {
            missingFields.add("At least one Experience entry");
        }

        JsonNode education = content.get("education");
        if (education == null || !education.isArray() || education.isEmpty()) {
            missingFields.add("At least one Education entry");
        }

        JsonNode skills = content.get("skills");
        if (skills == null || !skills.isArray() || skills.isEmpty()) {
            missingFields.add("At least one Skill");
        }

        return missingFields;
    }

    // ─────────────────────────────────────────────
    // Get raw Resume entity (used by export service)
    // ─────────────────────────────────────────────
    public Resume getRawResume(Integer resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));
    }

    // ─────────────────────────────────────────────
    // Helper: build empty content JSON for new resume
    // ─────────────────────────────────────────────
    private JsonNode buildEmptyContent() {
        String empty = """
                {
                  "personalInfo": {
                    "fullName": "",
                    "email": "",
                    "phone": "",
                    "location": "",
                    "linkedin": "",
                    "website": ""
                  },
                  "summary": "",
                  "experience": [],
                  "education": [],
                  "skills": [],
                  "certifications": []
                }
                """;
        try {
            return objectMapper.readTree(empty);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    // ─────────────────────────────────────────────
    // Helper: check if a JSON field is blank/missing
    // ─────────────────────────────────────────────
    private boolean isBlank(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return val == null || val.asText().isBlank();
    }

    // ─────────────────────────────────────────────
    // Helper: convert Resume entity → DTO
    // ─────────────────────────────────────────────
    private ResumeDTO toDTO(Resume resume) {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeId(resume.getResumeId());
        dto.setUserId(resume.getUserId());
        dto.setProfileId(resume.getProfileId());
        dto.setTitle(resume.getTitle());
        dto.setTemplate(resume.getTemplate());
        dto.setStatus(resume.getStatus());
        dto.setContent(resume.getContent());
        dto.setAiFeedback(resume.getAiFeedback());
        dto.setCreatedAt(resume.getCreatedAt());
        return dto;
    }
}