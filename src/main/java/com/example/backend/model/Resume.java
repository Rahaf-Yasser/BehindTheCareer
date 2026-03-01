package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "resume")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resumeId;

    private Integer userId;

    private Integer profileId;

    private String title;

    private String template;

    private String status;

    // ✅ JSON column mapping (Hibernate 6 way)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode content;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private LocalDateTime createdAt;


    // Constructor
    public Resume() {
        this.createdAt = LocalDateTime.now();
        this.status = "Draft";
    }


    // Getters & Setters

    public Integer getResumeId() {
        return resumeId;
    }

    public void setResumeId(Integer resumeId) {
        this.resumeId = resumeId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}