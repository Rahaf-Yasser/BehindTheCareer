package com.example.backend.controller;

import com.example.backend.dto.ResumeDTO;
import com.example.backend.model.Resume;
import com.example.backend.service.ExportService;
import com.example.backend.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ExportService exportService;

    // ─────────────────────────────────────────────
    // GET /api/resume/user/{userId}
    // ─────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDTO>> getResumesByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(resumeService.getResumesByUser(userId));
    }

    // ─────────────────────────────────────────────
    // GET /api/resume/{resumeId}
    // ─────────────────────────────────────────────
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeDTO> getResume(@PathVariable Integer resumeId) {
        return ResponseEntity.ok(resumeService.getResumeById(resumeId));
    }

    // ─────────────────────────────────────────────
    // POST /api/resume
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ResumeDTO> createResume(@RequestBody ResumeDTO dto) {
        ResumeDTO created = resumeService.createResume(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────────────────────────────────────
    // PUT /api/resume/{resumeId}/autosave
    // ─────────────────────────────────────────────
    @PutMapping("/{resumeId}/autosave")
    public ResponseEntity<ResumeDTO> autoSave(
            @PathVariable Integer resumeId,
            @RequestBody ResumeDTO dto) {
        return ResponseEntity.ok(resumeService.autoSave(resumeId, dto));
    }

    // ─────────────────────────────────────────────
    // DELETE /api/resume/{resumeId}
    // ─────────────────────────────────────────────
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(@PathVariable Integer resumeId) {
        resumeService.deleteResume(resumeId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    // POST /api/resume/{resumeId}/export/pdf
    // ─────────────────────────────────────────────
    @PostMapping("/{resumeId}/export/pdf")
    public ResponseEntity<?> exportPdf(@PathVariable Integer resumeId) {
        try {
            Resume resume = resumeService.getRawResume(resumeId);
            byte[] pdfBytes = exportService.exportToPdf(resume);

            String filename = (resume.getTitle() != null ? resume.getTitle() : "resume") + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate PDF: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // POST /api/resume/{resumeId}/export/docx
    // ─────────────────────────────────────────────
    @PostMapping("/{resumeId}/export/docx")
    public ResponseEntity<?> exportDocx(@PathVariable Integer resumeId) {
        try {
            Resume resume = resumeService.getRawResume(resumeId);
            byte[] docxBytes = exportService.exportToDocx(resume);

            String filename = (resume.getTitle() != null ? resume.getTitle() : "resume") + ".docx";
            String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(docxBytes);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate DOCX: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/resume/{resumeId}/validate
    // ─────────────────────────────────────────────
    @GetMapping("/{resumeId}/validate")
    public ResponseEntity<Map<String, Object>> validateResume(@PathVariable Integer resumeId) {
        List<String> missingFields = resumeService.validateForExport(resumeId);
        boolean isComplete = missingFields.isEmpty();

        return ResponseEntity.ok(Map.of(
                "isComplete", isComplete,
                "missingFields", missingFields
        ));
    }
}