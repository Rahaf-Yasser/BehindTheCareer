package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.security.UserDetailsImpl;
import com.example.backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    // ── GET /api/interview/options
    // Returns the user's profile fields + available durations [10, 15, 20]
    // Frontend uses this to populate the dropdowns before starting
    @GetMapping("/options")
    public ResponseEntity<ProfileFieldsResponse> getOptions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(interviewService.getAvailableOptions(userDetails.getUserId()));
    }

    // ── POST /api/interview/start
    // Body: { "selectedField": "Software Developer", "durationMinutes": 15 }
    // Returns session with all generated questions
    @PostMapping("/start")
    public ResponseEntity<InterviewSessionResponse> startInterview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody StartInterviewRequest request) {
        return ResponseEntity.ok(interviewService.startInterview(userDetails.getUserId(), request));
    }

    // ── POST /api/interview/{sessionId}/answer
    // Body: { "questionId": 1, "answerText": "My answer here..." }
    // Used when the frontend already transcribed the audio via Whisper
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<InterviewQuestionResponse> submitAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Integer sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(interviewService.submitAnswer(userDetails.getUserId(), sessionId, request));
    }

    // ── POST /api/interview/{sessionId}/answer/audio
    // Multipart: audioFile + questionId
    // Used when sending raw audio — backend transcribes via Whisper then analyzes
    @PostMapping("/{sessionId}/answer/audio")
    public ResponseEntity<InterviewQuestionResponse> submitAudioAnswer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Integer sessionId,
            @RequestParam("questionId") Integer questionId,
            @RequestParam("audioFile") MultipartFile audioFile) {
        return ResponseEntity.ok(
                interviewService.transcribeAndSubmit(userDetails.getUserId(), sessionId, questionId, audioFile));
    }

    // ── POST /api/interview/{sessionId}/finish
    // Ends the session and generates overall feedback + scores
    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<InterviewSessionResponse> finishInterview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Integer sessionId) {
        return ResponseEntity.ok(interviewService.finishInterview(userDetails.getUserId(), sessionId));
    }

    // ── GET /api/interview/{sessionId}
    // Get details of a specific session
    @GetMapping("/{sessionId}")
    public ResponseEntity<InterviewSessionResponse> getSession(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Integer sessionId) {
        return ResponseEntity.ok(interviewService.getSession(userDetails.getUserId(), sessionId));
    }

    // ── GET /api/interview/history
    // Get all past interview sessions for this user
    @GetMapping("/history")
    public ResponseEntity<List<InterviewSessionResponse>> getHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(interviewService.getAllSessions(userDetails.getUserId()));
    }
}