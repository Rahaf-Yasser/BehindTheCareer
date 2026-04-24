package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

        private final InterviewSessionRepository sessionRepository;
        private final InterviewQuestionRepository questionRepository;
        private final ProfileRepository profileRepository;
        private final UserRepository userRepository;
        private final OpenAIService openAIService;

        private static final List<Integer> ALLOWED_DURATIONS = List.of(10, 15, 20);

        // ── GET available fields and durations from user's profile ────────────────

        public ProfileFieldsResponse getAvailableOptions(Integer userId) {
                Profile profile = profileRepository.findByUser_UserId(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Profile not found. Please complete your profile first."));

                List<String> fields = parseFields(profile.getField());
                if (fields.isEmpty()) {
                        throw new RuntimeException(
                                        "No fields found in your profile. Please add your professional fields first.");
                }

                return ProfileFieldsResponse.builder()
                                .availableFields(fields)
                                .availableDurations(ALLOWED_DURATIONS)
                                .build();
        }

        // ── START interview session ───────────────────────────────────────────────

        @Transactional
        public InterviewSessionResponse startInterview(Integer userId, StartInterviewRequest request) {
                if (!ALLOWED_DURATIONS.contains(request.getDurationMinutes())) {
                        throw new RuntimeException("Duration must be 10, 15, or 20 minutes.");
                }

                Profile profile = profileRepository.findByUser_UserId(userId)
                                .orElseThrow(() -> new RuntimeException("Profile not found."));

                List<String> profileFields = parseFields(profile.getField());
                boolean fieldExists = profileFields.stream()
                                .anyMatch(f -> f.trim().equalsIgnoreCase(request.getSelectedField().trim()));

                if (!fieldExists) {
                        throw new RuntimeException("Selected field '" + request.getSelectedField()
                                        + "' is not in your profile. Your fields are: " + profileFields);
                }

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                InterviewSession session = InterviewSession.builder()
                                .user(user)
                                .selectedField(request.getSelectedField())
                                .durationMinutes(request.getDurationMinutes())
                                .status("IN_PROGRESS")
                                .build();

                session = sessionRepository.save(session);

                String skills = profile.getSkills() != null ? profile.getSkills() : "general";
                String expLevel = profile.getExperienceLevel() != null
                                ? profile.getExperienceLevel().toString()
                                : "INTERMEDIATE";

                List<String> generatedQuestions = openAIService.generateInterviewQuestions(
                                request.getSelectedField(), request.getDurationMinutes(), skills, expLevel);

                List<InterviewQuestion> savedQuestions = new ArrayList<>();
                for (int i = 0; i < generatedQuestions.size(); i++) {
                        InterviewQuestion q = InterviewQuestion.builder()
                                        .session(session)
                                        .questionOrder(i + 1)
                                        .questionText(generatedQuestions.get(i))
                                        .answered(false)
                                        .build();
                        savedQuestions.add(questionRepository.save(q));
                }

                session.setQuestions(savedQuestions);
                return toSessionResponse(session);
        }

        // ── SUBMIT answer (text already transcribed) ──────────────────────────────

        @Transactional
        public InterviewQuestionResponse submitAnswer(Integer userId, Integer sessionId, SubmitAnswerRequest request) {
                InterviewSession session = sessionRepository.findBySessionIdAndUser_UserId(sessionId, userId)
                                .orElseThrow(() -> new RuntimeException("Session not found"));

                if ("COMPLETED".equals(session.getStatus())) {
                        throw new RuntimeException("This interview session is already completed.");
                }

                InterviewQuestion question = questionRepository
                                .findByQuestionIdAndSession_SessionId(request.getQuestionId(), sessionId)
                                .orElseThrow(() -> new RuntimeException("Question not found in this session"));

                if (question.getAnswered()) {
                        throw new RuntimeException("This question has already been answered.");
                }

                question.setUserAnswer(request.getAnswerText());
                question.setAnswered(true);

                // Get full technical analysis from AI
                Map<String, Object> analysis = openAIService.analyzeAnswer(
                                question.getQuestionText(), request.getAnswerText(), session.getSelectedField());

                question.setAiFeedback((String) analysis.get("feedback"));
                question.setSuggestedImprovements((String) analysis.get("suggestedImprovements"));
                question.setCorrectness((String) analysis.get("correctness"));
                question.setCompleteness((String) analysis.get("completeness"));
                question.setRelevance((String) analysis.get("relevance"));
                question.setDepth((String) analysis.get("depth"));
                question.setScore(((Number) analysis.get("score")).doubleValue());

                questionRepository.save(question);
                return toQuestionResponse(question);
        }

        // ── TRANSCRIBE audio then submit answer ───────────────────────────────────

        @Transactional
        public InterviewQuestionResponse transcribeAndSubmit(Integer userId, Integer sessionId,
                        Integer questionId, MultipartFile audioFile) {
                String transcribedText = openAIService.transcribeAudio(audioFile);

                SubmitAnswerRequest answerRequest = new SubmitAnswerRequest();
                answerRequest.setQuestionId(questionId);
                answerRequest.setAnswerText(transcribedText);

                return submitAnswer(userId, sessionId, answerRequest);
        }

        // ── FINISH session and generate overall feedback ──────────────────────────

        @Transactional
        public InterviewSessionResponse finishInterview(Integer userId, Integer sessionId) {
                InterviewSession session = sessionRepository.findBySessionIdAndUser_UserId(sessionId, userId)
                                .orElseThrow(() -> new RuntimeException("Session not found"));

                if ("COMPLETED".equals(session.getStatus())) {
                        throw new RuntimeException("Session is already completed.");
                }

                List<InterviewQuestion> questions = questionRepository
                                .findBySession_SessionIdOrderByQuestionOrder(sessionId);

                List<String> questionTexts = questions.stream().map(InterviewQuestion::getQuestionText).toList();
                List<String> answers = questions.stream().map(InterviewQuestion::getUserAnswer).toList();
                List<Double> scores = questions.stream()
                                .map(q -> q.getScore() != null ? q.getScore() : 0.0).toList();

                Map<String, Object> overall = openAIService.generateOverallFeedback(
                                session.getSelectedField(), questionTexts, answers, scores);

                session.setStatus("COMPLETED");
                session.setCompletedAt(LocalDateTime.now());
                session.setOverallFeedback((String) overall.get("overallFeedback"));
                session.setStrongAreas((String) overall.get("strongAreas"));
                session.setWeakAreas((String) overall.get("weakAreas"));
                session.setStudyRecommendations((String) overall.get("studyRecommendations"));
                session.setOverallVerdict((String) overall.get("overallVerdict"));
                session.setAccuracyScore(((Number) overall.get("accuracyScore")).doubleValue());

                session = sessionRepository.save(session);
                session.setQuestions(questions);
                return toSessionResponse(session);
        }

        // ── GET session ───────────────────────────────────────────────────────────

        public InterviewSessionResponse getSession(Integer userId, Integer sessionId) {
                InterviewSession session = sessionRepository.findBySessionIdAndUser_UserId(sessionId, userId)
                                .orElseThrow(() -> new RuntimeException("Session not found"));
                List<InterviewQuestion> questions = questionRepository
                                .findBySession_SessionIdOrderByQuestionOrder(sessionId);
                session.setQuestions(questions);
                return toSessionResponse(session);
        }

        // ── GET all sessions history ──────────────────────────────────────────────

        public List<InterviewSessionResponse> getAllSessions(Integer userId) {
                return sessionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                                .stream()
                                .map(session -> {
                                        List<InterviewQuestion> questions = questionRepository
                                                        .findBySession_SessionIdOrderByQuestionOrder(
                                                                        session.getSessionId());
                                        session.setQuestions(questions);
                                        return toSessionResponse(session);
                                })
                                .collect(Collectors.toList());
        }

        // ── Helpers ───────────────────────────────────────────────────────────────

        private List<String> parseFields(String field) {
                if (field == null || field.isBlank())
                        return List.of();
                return Arrays.stream(field.split(","))
                                .map(String::trim)
                                .filter(f -> !f.isEmpty())
                                .collect(Collectors.toList());
        }

        private InterviewSessionResponse toSessionResponse(InterviewSession session) {
                List<InterviewQuestionResponse> questionResponses = session.getQuestions() != null
                                ? session.getQuestions().stream().map(this::toQuestionResponse)
                                                .collect(Collectors.toList())
                                : List.of();

                // Format dates as strings
                String createdAtStr = session.getCreatedAt() != null
                                ? session.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                                : null;
                String completedAtStr = session.getCompletedAt() != null
                                ? session.getCompletedAt().format(DateTimeFormatter.ISO_DATE_TIME)
                                : null;

                return InterviewSessionResponse.builder()
                                .sessionId(session.getSessionId())
                                .selectedField(session.getSelectedField())
                                .durationMinutes(session.getDurationMinutes())
                                .status(session.getStatus())
                                .questions(questionResponses)
                                .overallFeedback(session.getOverallFeedback())
                                .strongAreas(session.getStrongAreas())
                                .weakAreas(session.getWeakAreas())
                                .studyRecommendations(session.getStudyRecommendations())
                                .overallVerdict(session.getOverallVerdict())
                                .accuracyScore(session.getAccuracyScore())
                                .createdAt(createdAtStr) // Now passing String
                                .completedAt(completedAtStr) // Now passing String
                                .build();
        }

        private InterviewQuestionResponse toQuestionResponse(InterviewQuestion q) {
                return InterviewQuestionResponse.builder()
                                .questionId(q.getQuestionId())
                                .questionOrder(q.getQuestionOrder())
                                .questionText(q.getQuestionText())
                                .userAnswer(q.getUserAnswer())
                                .aiFeedback(q.getAiFeedback())
                                .suggestedImprovements(q.getSuggestedImprovements())
                                .correctness(q.getCorrectness())
                                .completeness(q.getCompleteness())
                                .relevance(q.getRelevance())
                                .depth(q.getDepth())
                                .score(q.getScore())
                                .answered(q.getAnswered())
                                .build();
        }
}