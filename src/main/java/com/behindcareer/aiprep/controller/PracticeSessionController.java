package com.behindcareer.aiprep.controller;

import com.behindcareer.aiprep.dto.BookmarkRequest;
import com.behindcareer.aiprep.dto.StartSessionRequest;
import com.behindcareer.aiprep.dto.SubmitSessionResponse;
import com.behindcareer.aiprep.dto.SubmitSessionRequest;
import com.behindcareer.aiprep.dto.FullSessionResponse;
import com.behindcareer.aiprep.entity.PracticeSession;
import com.behindcareer.aiprep.service.PracticeSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/practice")
public class PracticeSessionController {

    private final PracticeSessionService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public PracticeSessionController(PracticeSessionService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public FullSessionResponse startSession(@RequestBody StartSessionRequest request) {
        PracticeSession session = service.startSession(request);
        return buildResponse(session);
    }

    @PostMapping("/submit")
    public FullSessionResponse submitSession(@RequestBody SubmitSessionRequest request) {
        PracticeSession session = service.submitSession(request);
        return buildResponse(session);
    }

    @PostMapping("/bookmark")
    public FullSessionResponse bookmark(@RequestBody BookmarkRequest request) {
        PracticeSession session = service.bookmarkQuestion(request);
        return buildResponse(session);
    }

    @PostMapping("/remove-bookmark")
    public FullSessionResponse removeBookmark(@RequestBody BookmarkRequest request) {
        PracticeSession session = service.removeBookmark(request);
        return buildResponse(session);
    }

    @PostMapping("/end")
    public FullSessionResponse endSession(@RequestParam Long sessionId) {
        PracticeSession session = service.endSession(sessionId);
        return buildResponse(session);
    }

    @GetMapping("/{id}")
    public FullSessionResponse getSessionById(@PathVariable Long id) {
        PracticeSession session = service.getSessionById(id);
        return buildResponse(session);
    }

    @GetMapping("/test-gemini")
    public String testGemini() {
        return service.testGemini();
    }

    private FullSessionResponse buildResponse(PracticeSession session) {
        SubmitSessionResponse evaluation = new SubmitSessionResponse();
        
        try {
            // Check if evaluation exists in the database
            if (session.getEvaluationJson() != null && !session.getEvaluationJson().isEmpty()) {
                System.out.println("Evaluation found for session " + session.getId());
                // Parse the evaluation from the stored JSON
                Map<String, Object> fullEval = mapper.readValue(session.getEvaluationJson(), Map.class);
                Map<String, Object> evalMap = (Map<String, Object>) fullEval.get("evaluation");
                evaluation = mapper.convertValue(evalMap, SubmitSessionResponse.class);
            } else {
                System.out.println("No evaluation found for session " + session.getId());
                // No evaluation yet - return default values
                evaluation.setTotalScore(0);
                evaluation.setMaxScore(session.getNumberOfQuestions());
                evaluation.setOverallFeedback(null);
                evaluation.setQuestionsEvaluation(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            evaluation.setTotalScore(0);
            evaluation.setMaxScore(session.getNumberOfQuestions());
            evaluation.setOverallFeedback(null);
            evaluation.setQuestionsEvaluation(null);
        }
        
        // Create clean session without evaluationJson field
        PracticeSession cleanSession = createCleanSession(session);
        
        return new FullSessionResponse(cleanSession, evaluation);
    }
    
    private PracticeSession createCleanSession(PracticeSession original) {
        PracticeSession clean = new PracticeSession();
        clean.setId(original.getId());
        clean.setRole(original.getRole());
        clean.setLevel(original.getLevel());
        clean.setType(original.getType());
        clean.setNumberOfQuestions(original.getNumberOfQuestions());
        clean.setGeneratedQuestionsJson(original.getGeneratedQuestionsJson());
        clean.setUserAnswersJson(original.getUserAnswersJson());
        clean.setBookmarkedQuestions(original.getBookmarkedQuestions());
        clean.setCompleted(original.isCompleted());
        // Intentionally NOT setting evaluationJson to avoid duplication
        return clean;
    }
}