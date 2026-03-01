package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateId;

    private String name;
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String previewImage;
    private Boolean isActive;

    public Template() {}

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPreviewImage() { return previewImage; }
    public void setPreviewImage(String previewImage) { this.previewImage = previewImage; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}