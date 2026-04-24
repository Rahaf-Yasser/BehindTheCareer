package com.example.backend.controller;

import com.example.backend.dto.ResumeDTO;
import com.example.backend.model.Resume;
import com.example.backend.service.ExportService;
import com.example.backend.service.ResumeService;
import com.example.backend.service.ResumeUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private ResumeUploadService resumeUploadService;

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
    // ─────────────────────────────────────────────
    // POST /api/resume/upload
    // Upload a PDF or DOCX resume file
    // Params: userId, profileId (as request params)
    // Body: multipart file
    //
    // Example Postman:
    // POST http://localhost:8080/api/resume/upload?userId=1&profileId=1
    // Body → form-data → key: file, value: your file
    // ─────────────────────────────────────────────
    @PostMapping("/upload/{userId}/{profileId}")
    public ResponseEntity<?> uploadResume(
        @RequestParam("file") MultipartFile file,
        @PathVariable Integer userId,
        @PathVariable Integer profileId) {

        // Validate file is not empty
        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Please select a file to upload."));
        }

        // Validate file type
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".pdf") && !filename.endsWith(".docx"))) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Only PDF and DOCX files are supported."));
        }

        try {
            ResumeDTO result = resumeUploadService.uploadResume(file, userId, profileId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload resume: " + e.getMessage()));
        }
    }

}