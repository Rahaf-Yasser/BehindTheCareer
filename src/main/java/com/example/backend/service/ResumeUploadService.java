package com.example.backend.service;

import com.example.backend.dto.ResumeDTO;
import com.example.backend.model.Resume;
import com.example.backend.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeUploadService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────
    // MAIN UPLOAD METHOD
    // Detects file type, extracts text, saves resume
    // ─────────────────────────────────────────────
    public ResumeDTO uploadResume(MultipartFile file, Integer userId, Integer profileId) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Invalid file.");
        }

        String extractedText;

        if (filename.endsWith(".pdf")) {
            extractedText = extractFromPdf(file);
        } else if (filename.endsWith(".docx")) {
            extractedText = extractFromDocx(file);
        } else {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported.");
        }

        // Convert extracted text into structured JSON
        ObjectNode content = buildContentFromText(extractedText);

        // Save as a new resume
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setProfileId(profileId);
        resume.setTitle(filename.replace(".pdf", "").replace(".docx", ""));
        resume.setTemplate("classic");
        resume.setStatus("Draft");
        resume.setContent(content);
        resume.setCreatedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);

        // Return as DTO
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeId(saved.getResumeId());
        dto.setUserId(saved.getUserId());
        dto.setProfileId(saved.getProfileId());
        dto.setTitle(saved.getTitle());
        dto.setTemplate(saved.getTemplate());
        dto.setStatus(saved.getStatus());
        dto.setContent(saved.getContent());
        dto.setCreatedAt(saved.getCreatedAt());
        return dto;
    }

    // ─────────────────────────────────────────────
    // EXTRACT TEXT FROM PDF
    // ─────────────────────────────────────────────
    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // ─────────────────────────────────────────────
    // EXTRACT TEXT FROM DOCX
    // ─────────────────────────────────────────────
    private String extractFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();
                if (!text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString();
        }
    }

    // ─────────────────────────────────────────────
    // BUILD STRUCTURED JSON FROM EXTRACTED TEXT
    //
    // Since resumes don't have a fixed format, we do
    // our best to detect sections by common keywords.
    // The user can then edit any fields after uploading.
    // ─────────────────────────────────────────────
    private ObjectNode buildContentFromText(String text) {
        ObjectNode content = objectMapper.createObjectNode();

        // Personal Info — first few lines usually have name/contact
        ObjectNode personalInfo = objectMapper.createObjectNode();
        String[] lines = text.split("\n");

        // Try to find email
        String email = "";
        String phone = "";
        String name = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isBlank()) continue;

            if (name.isBlank() && line.length() > 2 && !line.contains("@") && !line.matches(".*\\d{5,}.*")) {
                name = line; // first non-blank line is likely the name
            }
            if (email.isBlank() && line.contains("@") && line.contains(".")) {
                email = extractEmail(line);
            }
            if (phone.isBlank() && line.matches(".*[\\d\\-\\+\\(\\)\\s]{7,}.*")) {
                phone = extractPhone(line);
            }
        }

        personalInfo.put("fullName", name);
        personalInfo.put("email", email);
        personalInfo.put("phone", phone);
        personalInfo.put("location", "");
        personalInfo.put("linkedin", "");
        personalInfo.put("website", "");
        content.set("personalInfo", personalInfo);

        // Summary
        content.put("summary", extractSection(text, new String[]{"summary", "objective", "profile", "about"}));

        // Experience
        ArrayNode experience = objectMapper.createArrayNode();
        String expText = extractSection(text, new String[]{"experience", "work experience", "employment"});
        if (!expText.isBlank()) {
            ObjectNode exp = objectMapper.createObjectNode();
            exp.put("id", 1);
            exp.put("company", "");
            exp.put("role", "");
            exp.put("startDate", "");
            exp.put("endDate", "");
            exp.put("current", false);
            exp.put("description", expText.trim());
            experience.add(exp);
        }
        content.set("experience", experience);

        // Education
        ArrayNode education = objectMapper.createArrayNode();
        String eduText = extractSection(text, new String[]{"education", "academic"});
        if (!eduText.isBlank()) {
            ObjectNode edu = objectMapper.createObjectNode();
            edu.put("id", 1);
            edu.put("school", "");
            edu.put("degree", "");
            edu.put("field", "");
            edu.put("graduationYear", "");
            education.add(edu);
        }
        content.set("education", education);

        // Skills
        ArrayNode skills = objectMapper.createArrayNode();
        String skillsText = extractSection(text, new String[]{"skills", "technical skills", "competencies"});
        if (!skillsText.isBlank()) {
            // Split by common delimiters
            String[] skillList = skillsText.split("[,•|\\n]+");
            for (String skill : skillList) {
                String s = skill.trim();
                if (!s.isBlank() && s.length() < 50) {
                    skills.add(s);
                }
            }
        }
        content.set("skills", skills);

        // Certifications
        content.set("certifications", objectMapper.createArrayNode());

        return content;
    }

    // ─────────────────────────────────────────────
    // Extract a section from text by keyword headers
    // ─────────────────────────────────────────────
    private String extractSection(String text, String[] keywords) {
        String lower = text.toLowerCase();

        for (String keyword : keywords) {
            int start = lower.indexOf(keyword);
            if (start != -1) {
                // Find next section header after this one
                int end = text.length();
                String[] commonHeaders = {"experience", "education", "skills", "summary",
                        "objective", "certifications", "projects", "references", "languages"};
                for (String header : commonHeaders) {
                    int pos = lower.indexOf(header, start + keyword.length() + 1);
                    if (pos != -1 && pos < end) {
                        end = pos;
                    }
                }
                String section = text.substring(start + keyword.length(), end).trim();
                if (!section.isBlank()) return section;
            }
        }
        return "";
    }

    // ─────────────────────────────────────────────
    // Simple email extractor from a line of text
    // ─────────────────────────────────────────────
    private String extractEmail(String line) {
        String[] parts = line.split("\\s+");
        for (String part : parts) {
            if (part.contains("@") && part.contains(".")) {
                return part.replaceAll("[,;]", "");
            }
        }
        return "";
    }

    // ─────────────────────────────────────────────
    // Simple phone extractor from a line of text
    // ─────────────────────────────────────────────
    private String extractPhone(String line) {
        return line.replaceAll("[^\\d\\+\\-\\(\\)\\s]", "").trim();
    }
}