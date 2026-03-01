package com.example.backend.service;

import com.example.backend.model.Resume;
import com.fasterxml.jackson.databind.JsonNode;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ExportService {

    // ─────────────────────────────────────────────
    // EXPORT TO PDF
    // ─────────────────────────────────────────────
    public byte[] exportToPdf(Resume resume) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode content = resume.getContent();
        JsonNode personalInfo = content.path("personalInfo");

        // Name
        doc.add(new Paragraph(personalInfo.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(20).setTextAlignment(TextAlignment.CENTER));

        // Contact line
        doc.add(new Paragraph(buildContactLine(personalInfo))
                .setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.CENTER));

        doc.add(new LineSeparator(new SolidLine()));

        // Summary
        String summary = content.path("summary").asText("");
        if (!summary.isBlank()) {
            doc.add(new Paragraph("SUMMARY").setFont(bold).setFontSize(12).setMarginTop(10));
            doc.add(new Paragraph(summary).setFont(regular).setFontSize(10));
            doc.add(new LineSeparator(new SolidLine()));
        }

        // Experience
        JsonNode experience = content.path("experience");
        if (experience.isArray() && !experience.isEmpty()) {
            doc.add(new Paragraph("EXPERIENCE").setFont(bold).setFontSize(12).setMarginTop(10));
            for (JsonNode exp : experience) {
                String role = exp.path("role").asText("") + " at " + exp.path("company").asText("");
                String dates = exp.path("startDate").asText("") + " - " +
                        (exp.path("current").asBoolean(false) ? "Present" : exp.path("endDate").asText(""));
                String description = exp.path("description").asText("");

                doc.add(new Paragraph(role).setFont(bold).setFontSize(10).setMarginTop(6));
                doc.add(new Paragraph(dates).setFont(regular).setFontSize(9).setFontColor(ColorConstants.GRAY));
                if (!description.isBlank()) {
                    doc.add(new Paragraph(description).setFont(regular).setFontSize(10));
                }
            }
            doc.add(new LineSeparator(new SolidLine()));
        }

        // Education
        JsonNode education = content.path("education");
        if (education.isArray() && !education.isEmpty()) {
            doc.add(new Paragraph("EDUCATION").setFont(bold).setFontSize(12).setMarginTop(10));
            for (JsonNode edu : education) {
                String degree = edu.path("degree").asText("") + " in " + edu.path("field").asText("");
                String school = edu.path("school").asText("") + " · " + edu.path("graduationYear").asText("");

                doc.add(new Paragraph(degree).setFont(bold).setFontSize(10).setMarginTop(6));
                doc.add(new Paragraph(school).setFont(regular).setFontSize(9));
            }
            doc.add(new LineSeparator(new SolidLine()));
        }

        // Skills
        JsonNode skills = content.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            doc.add(new Paragraph("SKILLS").setFont(bold).setFontSize(12).setMarginTop(10));
            StringBuilder sb = new StringBuilder();
            for (JsonNode skill : skills) {
                if (!sb.isEmpty()) sb.append(" · ");
                sb.append(skill.asText());
            }
            doc.add(new Paragraph(sb.toString()).setFont(regular).setFontSize(10));
        }

        // Certifications
        JsonNode certs = content.path("certifications");
        if (certs.isArray() && !certs.isEmpty()) {
            doc.add(new LineSeparator(new SolidLine()));
            doc.add(new Paragraph("CERTIFICATIONS").setFont(bold).setFontSize(12).setMarginTop(10));
            for (JsonNode cert : certs) {
                String line = cert.path("name").asText("") +
                        (cert.path("issuer").asText("").isBlank() ? "" : " - " + cert.path("issuer").asText("")) +
                        (cert.path("year").asText("").isBlank() ? "" : " (" + cert.path("year").asText("") + ")");
                doc.add(new Paragraph(line).setFont(regular).setFontSize(10));
            }
        }

        doc.close();
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────
    // EXPORT TO DOCX
    // ─────────────────────────────────────────────
    public byte[] exportToDocx(Resume resume) throws Exception {
        XWPFDocument document = new XWPFDocument();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonNode content = resume.getContent();
        JsonNode personalInfo = content.path("personalInfo");

        // Name
        XWPFParagraph namePara = document.createParagraph();
        namePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun nameRun = namePara.createRun();
        nameRun.setText(personalInfo.path("fullName").asText("Your Name"));
        nameRun.setBold(true);
        nameRun.setFontSize(20);

        // Contact line
        XWPFParagraph contactPara = document.createParagraph();
        contactPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun contactRun = contactPara.createRun();
        contactRun.setText(buildContactLine(personalInfo));
        contactRun.setFontSize(10);

        addDivider(document);

        // Summary
        String summary = content.path("summary").asText("");
        if (!summary.isBlank()) {
            addSectionHeader(document, "SUMMARY");
            addBody(document, summary);
            addDivider(document);
        }

        // Experience
        JsonNode experience = content.path("experience");
        if (experience.isArray() && !experience.isEmpty()) {
            addSectionHeader(document, "EXPERIENCE");
            for (JsonNode exp : experience) {
                String role = exp.path("role").asText("") + " at " + exp.path("company").asText("");
                String dates = exp.path("startDate").asText("") + " - " +
                        (exp.path("current").asBoolean(false) ? "Present" : exp.path("endDate").asText(""));
                String desc = exp.path("description").asText("");

                addBold(document, role);
                addSmall(document, dates);
                if (!desc.isBlank()) addBody(document, desc);
            }
            addDivider(document);
        }

        // Education
        JsonNode education = content.path("education");
        if (education.isArray() && !education.isEmpty()) {
            addSectionHeader(document, "EDUCATION");
            for (JsonNode edu : education) {
                String degree = edu.path("degree").asText("") + " in " + edu.path("field").asText("");
                String school = edu.path("school").asText("") + " · " + edu.path("graduationYear").asText("");
                addBold(document, degree);
                addSmall(document, school);
            }
            addDivider(document);
        }

        // Skills
        JsonNode skills = content.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            addSectionHeader(document, "SKILLS");
            StringBuilder sb = new StringBuilder();
            for (JsonNode skill : skills) {
                if (!sb.isEmpty()) sb.append(" · ");
                sb.append(skill.asText());
            }
            addBody(document, sb.toString());
        }

        // Certifications
        JsonNode certs = content.path("certifications");
        if (certs.isArray() && !certs.isEmpty()) {
            addDivider(document);
            addSectionHeader(document, "CERTIFICATIONS");
            for (JsonNode cert : certs) {
                String line = cert.path("name").asText("") +
                        (cert.path("issuer").asText("").isBlank() ? "" : " - " + cert.path("issuer").asText("")) +
                        (cert.path("year").asText("").isBlank() ? "" : " (" + cert.path("year").asText("") + ")");
                addBody(document, line);
            }
        }

        document.write(baos);
        document.close();
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private String buildContactLine(JsonNode personalInfo) {
        StringBuilder sb = new StringBuilder();
        appendField(sb, personalInfo, "email");
        appendField(sb, personalInfo, "phone");
        appendField(sb, personalInfo, "location");
        appendField(sb, personalInfo, "linkedin");
        appendField(sb, personalInfo, "website");
        return sb.toString();
    }

    private void appendField(StringBuilder sb, JsonNode node, String field) {
        String val = node.path(field).asText("").trim();
        if (!val.isBlank()) {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append(val);
        }
    }

    private void addSectionHeader(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(12);
    }

    private void addBold(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(10);
    }

    private void addBody(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(10);
    }

    private void addSmall(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(9);
        r.setColor("888888");
    }

    private void addDivider(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setBorderBottom(Borders.SINGLE);
    }
}