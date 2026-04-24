package com.example.backend.controller;

import com.example.backend.model.AIResumeAnalyzer;
import com.example.backend.service.ResumeAnalyzerService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume/analyze")
@CrossOrigin(origins = "*")
public class ResumeAnalyzerController {

    @Autowired
    private ResumeAnalyzerService analyzerService;

    // ─────────────────────────────────────────────
    // POST /api/resume/analyze/{resumeId}
    // Analyze a resume using Gemini AI
    // ─────────────────────────────────────────────
    @PostMapping("/{resumeId}")
    public ResponseEntity<?> analyzeResume(@PathVariable Integer resumeId) {
        try {
            JsonNode analysis = analyzerService.analyzeResume(resumeId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to analyze resume: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/resume/analyze/{resumeId}/history
    // Get all previous analyses for a resume
    // ─────────────────────────────────────────────
    @GetMapping("/{resumeId}/history")
    public ResponseEntity<List<AIResumeAnalyzer>> getAnalysisHistory(@PathVariable Integer resumeId) {
        return ResponseEntity.ok(analyzerService.getAnalysesForResume(resumeId));
    }
}