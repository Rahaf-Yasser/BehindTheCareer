package com.example.backend.service;

import com.example.backend.model.Resume;
import com.fasterxml.jackson.databind.JsonNode;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.properties.TabAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ExportService {

    // Colors
    private static final DeviceRgb NAVY       = new DeviceRgb(0,   31,  63);
    private static final DeviceRgb TEAL       = new DeviceRgb(0,  128, 128);
    private static final DeviceRgb CHARCOAL   = new DeviceRgb(54,  54,  54);
    private static final DeviceRgb ORANGE     = new DeviceRgb(230, 100,  20);
    private static final DeviceRgb PURPLE     = new DeviceRgb(90,   40, 160);
    private static final DeviceRgb GREEN      = new DeviceRgb(30,  130,  76);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(240, 240, 240);

    // ─────────────────────────────────────────────
    // EXPORT TO PDF — routes to correct template
    // ─────────────────────────────────────────────
    public byte[] exportToPdf(Resume resume) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        String template = resume.getTemplate() != null ? resume.getTemplate() : "classic";
        JsonNode content = resume.getContent();

        switch (template) {
            case "modern"           -> renderModern(doc, content, resume);
            case "minimal"          -> renderMinimal(doc, content, resume);
            case "fullstack-dev"    -> renderFullstack(doc, content, resume);
            case "backend-engineer" -> renderBackend(doc, content, resume);
            case "frontend-dev"     -> renderFrontend(doc, content, resume);
            default                 -> renderClassic(doc, content, resume);
        }

        doc.close();
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────
    // 1. CLASSIC — centered header, Times font, ruled lines
    // ─────────────────────────────────────────────
    private void renderClassic(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont italic  = PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC);

        JsonNode pi = content.path("personalInfo");

        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(22).setTextAlignment(TextAlignment.CENTER).setFontColor(CHARCOAL));
        doc.add(new Paragraph(buildContactLine(pi))
                .setFont(italic).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY));
        doc.add(new LineSeparator(new SolidLine()));

        addSection(doc, "SUMMARY",        content.path("summary").asText(""),        bold, regular, CHARCOAL);
        addExperience(doc, content,   bold, regular, italic, CHARCOAL);
        addEducation(doc, content,    bold, regular, CHARCOAL);
        addSkills(doc, content,       bold, regular, CHARCOAL);
        addCertifications(doc, content, bold, regular, CHARCOAL);
    }

    // ─────────────────────────────────────────────
    // 2. MODERN — navy banner header, Helvetica, accent color
    // ─────────────────────────────────────────────
    private void renderModern(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode pi = content.path("personalInfo");

        // Navy banner
        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(24)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(NAVY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(14).setMarginBottom(4));

        doc.add(new Paragraph(buildContactLine(pi))
                .setFont(regular).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(NAVY).setMarginBottom(10));

        addSection(doc, "SUMMARY",        content.path("summary").asText(""),        bold, regular, NAVY);
        addExperience(doc, content,   bold, regular, regular, NAVY);
        addEducation(doc, content,    bold, regular, NAVY);
        addSkills(doc, content,       bold, regular, NAVY);
        addCertifications(doc, content, bold, regular, NAVY);
    }

    // ─────────────────────────────────────────────
    // 3. MINIMAL — left-aligned, lots of whitespace, dashed dividers
    // ─────────────────────────────────────────────
    private void renderMinimal(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode pi = content.path("personalInfo");

        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(26)
                .setFontColor(CHARCOAL)
                .setMarginBottom(2));

        doc.add(new Paragraph(buildContactLine(pi))
                .setFont(regular).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginBottom(12));

        doc.add(new LineSeparator(new DashedLine()));

        addMinimalSection(doc, "Summary",        content.path("summary").asText(""),   bold, regular);
        addMinimalExperience(doc, content, bold, regular);
        addMinimalEducation(doc, content,  bold, regular);
        addMinimalSkills(doc, content,     bold, regular);
    }

    // ─────────────────────────────────────────────
    // 4. FULLSTACK-DEV — teal accent, two skill columns (Frontend / Backend)
    // ─────────────────────────────────────────────
    private void renderFullstack(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode pi = content.path("personalInfo");

        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(22)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(TEAL)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(12).setMarginBottom(4));

        doc.add(new Paragraph("Full Stack Developer  |  " + buildContactLine(pi))
                .setFont(regular).setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(TEAL).setMarginBottom(10));

        addSection(doc, "SUMMARY",  content.path("summary").asText(""), bold, regular, TEAL);
        addExperience(doc, content, bold, regular, regular, TEAL);
        addEducation(doc, content,  bold, regular, TEAL);

        // Skills split into Frontend / Backend
        doc.add(sectionHeader("TECHNICAL SKILLS", bold, TEAL));
        JsonNode skills = content.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            int total = skills.size();
            int half  = total / 2;
            StringBuilder front = new StringBuilder("Frontend:  ");
            StringBuilder back  = new StringBuilder("Backend:   ");
            for (int i = 0; i < total; i++) {
                if (i < half) { if (i > 0) front.append(" · "); front.append(skills.get(i).asText()); }
                else          { if (i > half) back.append(" · "); back.append(skills.get(i).asText()); }
            }
            doc.add(new Paragraph(front.toString()).setFont(bold).setFontSize(10).setFontColor(TEAL));
            doc.add(new Paragraph(back.toString()).setFont(regular).setFontSize(10));
        }
        addCertifications(doc, content, bold, regular, TEAL);
    }

    // ─────────────────────────────────────────────
    // 5. BACKEND-ENGINEER — orange accent, highlights architecture & systems
    // ─────────────────────────────────────────────
    private void renderBackend(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode pi = content.path("personalInfo");

        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(22)
                .setFontColor(CHARCOAL)
                .setMarginBottom(2));

        doc.add(new Paragraph("Backend Engineer")
                .setFont(bold).setFontSize(13)
                .setFontColor(ORANGE).setMarginBottom(2));

        doc.add(new Paragraph(buildContactLine(pi))
                .setFont(regular).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginBottom(8));

        doc.add(new LineSeparator(new SolidLine(1f)));

        addSection(doc, "PROFILE",     content.path("summary").asText(""),   bold, regular, ORANGE);
        addExperience(doc, content,    bold, regular, regular, ORANGE);
        addEducation(doc, content,     bold, regular, ORANGE);

        // Skills with "Core Technologies" label
        doc.add(sectionHeader("CORE TECHNOLOGIES", bold, ORANGE));
        JsonNode skills = content.path("skills");
        StringBuilder sb = new StringBuilder();
        if (skills.isArray()) {
            for (JsonNode s : skills) { if (!sb.isEmpty()) sb.append("  ·  "); sb.append(s.asText()); }
        }
        doc.add(new Paragraph(sb.toString()).setFont(regular).setFontSize(10).setMarginBottom(6));
        addCertifications(doc, content, bold, regular, ORANGE);
    }

    // ─────────────────────────────────────────────
    // 6. FRONTEND-DEV — purple accent, highlights frameworks & UI portfolio
    // ─────────────────────────────────────────────
    private void renderFrontend(Document doc, JsonNode content, Resume resume) throws Exception {
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        JsonNode pi = content.path("personalInfo");

        doc.add(new Paragraph(pi.path("fullName").asText("Your Name"))
                .setFont(bold).setFontSize(22)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PURPLE)
                .setTextAlignment(TextAlignment.LEFT)
                .setPaddingLeft(16).setPadding(12).setMarginBottom(2));

        doc.add(new Paragraph("Frontend Developer  |  " + buildContactLine(pi))
                .setFont(regular).setFontSize(9)
                .setFontColor(PURPLE).setMarginBottom(10));

        addSection(doc, "ABOUT ME",    content.path("summary").asText(""),   bold, regular, PURPLE);
        addExperience(doc, content,    bold, regular, regular, PURPLE);
        addEducation(doc, content,     bold, regular, PURPLE);

        // Skills labeled as UI Frameworks & Tools
        doc.add(sectionHeader("UI FRAMEWORKS & TOOLS", bold, PURPLE));
        JsonNode skills = content.path("skills");
        StringBuilder sb = new StringBuilder();
        if (skills.isArray()) {
            for (JsonNode s : skills) { if (!sb.isEmpty()) sb.append("  ·  "); sb.append(s.asText()); }
        }
        doc.add(new Paragraph(sb.toString()).setFont(regular).setFontSize(10).setMarginBottom(6));

        // Portfolio link if website provided
        String website = pi.path("website").asText("");
        if (!website.isBlank()) {
            doc.add(sectionHeader("PORTFOLIO", bold, PURPLE));
            doc.add(new Paragraph(website).setFont(regular).setFontSize(10).setFontColor(PURPLE));
        }
        addCertifications(doc, content, bold, regular, PURPLE);
    }

    // ─────────────────────────────────────────────
    // SHARED SECTION RENDERERS
    // ─────────────────────────────────────────────
    private void addSection(Document doc, String title, String body,
                            PdfFont bold, PdfFont regular, DeviceRgb color) {
        if (body.isBlank()) return;
        doc.add(sectionHeader(title, bold, color));
        doc.add(new Paragraph(body).setFont(regular).setFontSize(10).setMarginBottom(6));
        doc.add(new LineSeparator(new SolidLine()));
    }

    private void addExperience(Document doc, JsonNode content,
                               PdfFont bold, PdfFont regular, PdfFont italic, DeviceRgb color) {
        JsonNode exp = content.path("experience");
        if (!exp.isArray() || exp.isEmpty()) return;
        doc.add(sectionHeader("EXPERIENCE", bold, color));
        for (JsonNode e : exp) {
            String role    = e.path("role").asText("");
            String company = e.path("company").asText("");
            String dates   = e.path("startDate").asText("") + " – " +
                    (e.path("current").asBoolean(false) ? "Present" : e.path("endDate").asText(""));
            String desc    = e.path("description").asText("");

            doc.add(new Paragraph(role + " — " + company)
                    .setFont(bold).setFontSize(11).setMarginTop(6).setFontColor(color));
            doc.add(new Paragraph(dates)
                    .setFont(italic).setFontSize(9).setFontColor(ColorConstants.GRAY));
            if (!desc.isBlank())
                doc.add(new Paragraph(desc).setFont(regular).setFontSize(10).setMarginBottom(4));
        }
        doc.add(new LineSeparator(new SolidLine()));
    }

    private void addEducation(Document doc, JsonNode content,
                              PdfFont bold, PdfFont regular, DeviceRgb color) {
        JsonNode edu = content.path("education");
        if (!edu.isArray() || edu.isEmpty()) return;
        doc.add(sectionHeader("EDUCATION", bold, color));
        for (JsonNode e : edu) {
            String degree = e.path("degree").asText("") + " in " + e.path("field").asText("");
            String school = e.path("school").asText("") + "  ·  " + e.path("graduationYear").asText("");
            doc.add(new Paragraph(degree).setFont(bold).setFontSize(10).setMarginTop(4).setFontColor(color));
            doc.add(new Paragraph(school).setFont(regular).setFontSize(9).setFontColor(ColorConstants.GRAY));
        }
        doc.add(new LineSeparator(new SolidLine()));
    }

    private void addSkills(Document doc, JsonNode content,
                           PdfFont bold, PdfFont regular, DeviceRgb color) {
        JsonNode skills = content.path("skills");
        if (!skills.isArray() || skills.isEmpty()) return;
        doc.add(sectionHeader("SKILLS", bold, color));
        StringBuilder sb = new StringBuilder();
        for (JsonNode s : skills) { if (!sb.isEmpty()) sb.append("  ·  "); sb.append(s.asText()); }
        doc.add(new Paragraph(sb.toString()).setFont(regular).setFontSize(10).setMarginBottom(6));
    }

    private void addCertifications(Document doc, JsonNode content,
                                   PdfFont bold, PdfFont regular, DeviceRgb color) {
        JsonNode certs = content.path("certifications");
        if (!certs.isArray() || certs.isEmpty()) return;
        doc.add(new LineSeparator(new SolidLine()));
        doc.add(sectionHeader("CERTIFICATIONS", bold, color));
        for (JsonNode c : certs) {
            String line = c.path("name").asText("") +
                    (c.path("issuer").asText("").isBlank() ? "" : " – " + c.path("issuer").asText("")) +
                    (c.path("year").asText("").isBlank()   ? "" : " (" + c.path("year").asText("") + ")");
            doc.add(new Paragraph(line).setFont(regular).setFontSize(10));
        }
    }

    // Minimal template helpers (dashed lines, smaller text)
    private void addMinimalSection(Document doc, String title, String body,
                                   PdfFont bold, PdfFont regular) {
        if (body.isBlank()) return;
        doc.add(new Paragraph(title.toUpperCase()).setFont(bold).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginTop(10).setMarginBottom(2));
        doc.add(new Paragraph(body).setFont(regular).setFontSize(10).setMarginBottom(4));
        doc.add(new LineSeparator(new DashedLine()));
    }

    private void addMinimalExperience(Document doc, JsonNode content, PdfFont bold, PdfFont regular) {
        JsonNode exp = content.path("experience");
        if (!exp.isArray() || exp.isEmpty()) return;
        doc.add(new Paragraph("EXPERIENCE").setFont(bold).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginTop(10).setMarginBottom(2));
        for (JsonNode e : exp) {
            doc.add(new Paragraph(e.path("role").asText("") + "  ·  " + e.path("company").asText(""))
                    .setFont(bold).setFontSize(10).setMarginTop(4));
            doc.add(new Paragraph(e.path("startDate").asText("") + " – " +
                    (e.path("current").asBoolean(false) ? "Present" : e.path("endDate").asText("")))
                    .setFont(regular).setFontSize(9).setFontColor(ColorConstants.GRAY));
            String desc = e.path("description").asText("");
            if (!desc.isBlank())
                doc.add(new Paragraph(desc).setFont(regular).setFontSize(10));
        }
        doc.add(new LineSeparator(new DashedLine()));
    }

    private void addMinimalEducation(Document doc, JsonNode content, PdfFont bold, PdfFont regular) {
        JsonNode edu = content.path("education");
        if (!edu.isArray() || edu.isEmpty()) return;
        doc.add(new Paragraph("EDUCATION").setFont(bold).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginTop(10).setMarginBottom(2));
        for (JsonNode e : edu) {
            doc.add(new Paragraph(e.path("degree").asText("") + " in " + e.path("field").asText(""))
                    .setFont(bold).setFontSize(10).setMarginTop(4));
            doc.add(new Paragraph(e.path("school").asText("")).setFont(regular).setFontSize(9));
        }
        doc.add(new LineSeparator(new DashedLine()));
    }

    private void addMinimalSkills(Document doc, JsonNode content, PdfFont bold, PdfFont regular) {
        JsonNode skills = content.path("skills");
        if (!skills.isArray() || skills.isEmpty()) return;
        doc.add(new Paragraph("SKILLS").setFont(bold).setFontSize(9)
                .setFontColor(ColorConstants.GRAY).setMarginTop(10).setMarginBottom(2));
        StringBuilder sb = new StringBuilder();
        for (JsonNode s : skills) { if (!sb.isEmpty()) sb.append("  ·  "); sb.append(s.asText()); }
        doc.add(new Paragraph(sb.toString()).setFont(regular).setFontSize(10));
    }

    private Paragraph sectionHeader(String title, PdfFont bold, DeviceRgb color) {
        return new Paragraph(title)
                .setFont(bold).setFontSize(12)
                .setFontColor(color)
                .setMarginTop(10).setMarginBottom(4);
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

        String summary = content.path("summary").asText("");
        if (!summary.isBlank()) {
            addSectionHeader(document, "SUMMARY");
            addBody(document, summary);
            addDivider(document);
        }

        JsonNode experience = content.path("experience");
        if (experience.isArray() && !experience.isEmpty()) {
            addSectionHeader(document, "EXPERIENCE");
            for (JsonNode exp : experience) {
                addBold(document, exp.path("role").asText("") + " at " + exp.path("company").asText(""));
                addSmall(document, exp.path("startDate").asText("") + " – " +
                        (exp.path("current").asBoolean(false) ? "Present" : exp.path("endDate").asText("")));
                String desc = exp.path("description").asText("");
                if (!desc.isBlank()) addBody(document, desc);
            }
            addDivider(document);
        }

        JsonNode education = content.path("education");
        if (education.isArray() && !education.isEmpty()) {
            addSectionHeader(document, "EDUCATION");
            for (JsonNode edu : education) {
                addBold(document, edu.path("degree").asText("") + " in " + edu.path("field").asText(""));
                addSmall(document, edu.path("school").asText("") + " · " + edu.path("graduationYear").asText(""));
            }
            addDivider(document);
        }

        JsonNode skills = content.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            addSectionHeader(document, "SKILLS");
            StringBuilder sb = new StringBuilder();
            for (JsonNode skill : skills) { if (!sb.isEmpty()) sb.append(" · "); sb.append(skill.asText()); }
            addBody(document, sb.toString());
        }

        JsonNode certs = content.path("certifications");
        if (certs.isArray() && !certs.isEmpty()) {
            addDivider(document);
            addSectionHeader(document, "CERTIFICATIONS");
            for (JsonNode cert : certs) {
                String line = cert.path("name").asText("") +
                        (cert.path("issuer").asText("").isBlank() ? "" : " – " + cert.path("issuer").asText("")) +
                        (cert.path("year").asText("").isBlank()   ? "" : " (" + cert.path("year").asText("") + ")");
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
        if (!val.isBlank()) { if (!sb.isEmpty()) sb.append(" | "); sb.append(val); }
    }

    private void addSectionHeader(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setText(text); r.setBold(true); r.setFontSize(12);
    }

    private void addBold(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setBold(true); r.setFontSize(10);
    }

    private void addBody(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(10);
    }

    private void addSmall(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text); r.setFontSize(9); r.setColor("888888");
    }

    private void addDivider(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setBorderBottom(Borders.SINGLE);
    }
}