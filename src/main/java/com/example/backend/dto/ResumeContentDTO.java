package com.example.backend.dto;

import java.util.List;
import java.util.Map;

public class ResumeContentDTO {

    private PersonalInfoDTO personalInfo;
    private String summary;

    private List<Map<String, Object>> education;
    private List<Map<String, Object>> experience;
    private List<String> skills;
    private List<String> languages;

    private List<Map<String, Object>> projects;
    private List<Map<String, Object>> certifications;
    private List<Map<String, Object>> links;

    private Map<String, Object> extraFields;

    public ResumeContentDTO() {
    }

    public PersonalInfoDTO getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfoDTO personalInfo) {
        this.personalInfo = personalInfo;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Map<String, Object>> getEducation() {
        return education;
    }

    public void setEducation(List<Map<String, Object>> education) {
        this.education = education;
    }

    public List<Map<String, Object>> getExperience() {
        return experience;
    }

    public void setExperience(List<Map<String, Object>> experience) {
        this.experience = experience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<Map<String, Object>> getProjects() {
        return projects;
    }

    public void setProjects(List<Map<String, Object>> projects) {
        this.projects = projects;
    }

    public List<Map<String, Object>> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<Map<String, Object>> certifications) {
        this.certifications = certifications;
    }

    public List<Map<String, Object>> getLinks() {
        return links;
    }

    public void setLinks(List<Map<String, Object>> links) {
        this.links = links;
    }

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }
}