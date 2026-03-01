package com.example.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * Used for both incoming requests (create/update) and outgoing responses.
 * The `content` field holds the full JSON resume data.
 */
public class ResumeDTO {

    private Integer resumeId;       // null on create, populated on response
    private Integer userId;
    private Integer profileId;
    private String title;
    private String template;        // e.g. "classic", "modern", "minimal"
    private String status;          // "Draft" or "Complete"
    private JsonNode content;       // Full resume JSON (see structure below)
    private String aiFeedback;
    private LocalDateTime createdAt;

    /*
     * Expected `content` JSON structure:
     * {
     *   "personalInfo": {
     *     "fullName": "",
     *     "email": "",
     *     "phone": "",
     *     "location": "",
     *     "linkedin": "",
     *     "website": ""
     *   },
     *   "summary": "",
     *   "experience": [
     *     { "id": 1, "company": "", "role": "", "startDate": "", "endDate": "", "description": "", "current": false }
     *   ],
     *   "education": [
     *     { "id": 1, "school": "", "degree": "", "field": "", "graduationYear": "" }
     *   ],
     *   "skills": ["Java", "React"],
     *   "certifications": [
     *     { "id": 1, "name": "", "issuer": "", "year": "" }
     *   ]
     * }
     */

    public ResumeDTO() {}

    // --- Getters & Setters ---

    public Integer getResumeId() { return resumeId; }
    public void setResumeId(Integer resumeId) { this.resumeId = resumeId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public JsonNode getContent() { return content; }
    public void setContent(JsonNode content) { this.content = content; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}