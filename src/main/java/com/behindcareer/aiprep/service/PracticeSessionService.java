package com.behindcareer.aiprep.service;

import com.behindcareer.aiprep.dto.BookmarkRequest;
import com.behindcareer.aiprep.dto.StartSessionRequest;
import com.behindcareer.aiprep.dto.SubmitSessionRequest;
import com.behindcareer.aiprep.entity.PracticeSession;
import com.behindcareer.aiprep.repository.PracticeSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PracticeSessionService {

    private final PracticeSessionRepository repository;
    private final GeminiService geminiService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PracticeSessionService(PracticeSessionRepository repository,
                                  GeminiService geminiService) {
        this.repository = repository;
        this.geminiService = geminiService;
    }

    @Transactional
    public PracticeSession startSession(StartSessionRequest request) {
        String questionsJson = geminiService.generateQuestions(
            request.getRole(),
            request.getLevel(),
            request.getType(),
            request.getNumberOfQuestions()
        );

        PracticeSession session = new PracticeSession();
        session.setRole(request.getRole());
        session.setLevel(request.getLevel());
        session.setType(request.getType());
        session.setNumberOfQuestions(request.getNumberOfQuestions());
        session.setGeneratedQuestionsJson(questionsJson);
        session.setCompleted(false);
        session.setUserAnswersJson(null);
        session.setBookmarkedQuestions(null);

        return repository.save(session);
    }

    @Transactional
    public PracticeSession submitSession(SubmitSessionRequest request) {
        PracticeSession session = getSessionById(request.getSessionId());
        
        Boolean forceEnd = request.getForceEnd() != null && request.getForceEnd();
        
        String evaluationJson = geminiService.evaluateAnswers(
            session.getGeneratedQuestionsJson(),
            request.getAnswersJson(),
            session.getId(),
            forceEnd,
            session.getNumberOfQuestions(),
            session.getEvaluationJson()
        );
        
        session.setUserAnswersJson(request.getAnswersJson());
        session.setEvaluationJson(evaluationJson);
        
        try {
            Map<String, Object> eval = mapper.readValue(evaluationJson, Map.class);
            Map<String, Object> sess = (Map<String, Object>) eval.get("session");
            Boolean completed = (Boolean) sess.get("completed");
            session.setCompleted(completed != null && completed);
        } catch (Exception e) {
            session.setCompleted(forceEnd);
        }
        
        return repository.save(session);
    }

    @Transactional
    public PracticeSession bookmarkQuestion(BookmarkRequest request) {
        PracticeSession session = getSessionById(request.getSessionId());
        
        String value = String.valueOf(request.getQuestionNumber());
        String current = session.getBookmarkedQuestions();
        
        // Handle bookmarkAllPrevious
        if (request.getBookmarkAllPrevious() != null && request.getBookmarkAllPrevious()) {
            Set<String> allBookmarks = new LinkedHashSet<>();
            for (int i = 1; i <= request.getQuestionNumber(); i++) {
                allBookmarks.add(String.valueOf(i));
            }
            session.setBookmarkedQuestions(String.join(",", allBookmarks));
            return repository.save(session);
        }
        
        // Regular bookmark - use Set to avoid duplicates
        Set<String> bookmarks = new LinkedHashSet<>();
        if (current != null && !current.isBlank()) {
            bookmarks.addAll(Arrays.asList(current.split(",")));
        }
        
        bookmarks.add(value);
        session.setBookmarkedQuestions(String.join(",", bookmarks));
        
        return repository.save(session);
    }

    @Transactional
    public PracticeSession removeBookmark(BookmarkRequest request) {
        PracticeSession session = getSessionById(request.getSessionId());
        
        // If questionNumber is null or 0, remove ALL bookmarks
        if (request.getQuestionNumber() == null || request.getQuestionNumber() == 0) {
            session.setBookmarkedQuestions(null);
            return repository.save(session);
        }
        
        // Otherwise, remove only the specific question
        String value = String.valueOf(request.getQuestionNumber());
        String current = session.getBookmarkedQuestions();
        
        if (current != null && !current.isBlank()) {
            List<String> bookmarksList = new ArrayList<>(Arrays.asList(current.split(",")));
            bookmarksList.removeIf(bookmark -> bookmark.equals(value));
            
            if (bookmarksList.isEmpty()) {
                session.setBookmarkedQuestions(null);
            } else {
                session.setBookmarkedQuestions(String.join(",", bookmarksList));
            }
        }
        
        return repository.save(session);
    }

    @Transactional
    public PracticeSession endSession(Long sessionId) {
        PracticeSession session = getSessionById(sessionId);
        session.setCompleted(true);
        return repository.save(session);
    }

    public PracticeSession getSessionById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    public String testGemini() {
        return geminiService.generateQuestions("Software Engineer", "Junior", "mcq", 1);
    }
}