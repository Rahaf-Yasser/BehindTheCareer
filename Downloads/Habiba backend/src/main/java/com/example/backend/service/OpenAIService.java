package com.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
public class OpenAIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    // ── Core AI call using Groq API ───────────────────────────────────────────

    private String generateContent(String prompt) {
        try {
            System.out.println("📡 Calling Groq API...");
            String result = callGroq(prompt);
            System.out.println("✅ Groq API responded successfully");
            return result;
        } catch (Exception e) {
            System.out.println("❌ Groq API failed: " + e.getMessage());
            throw new RuntimeException("Groq API failed: " + e.getMessage());
        }
    }

    private String callGroq(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("messages", List.of(message));
        body.put("temperature", 0.7);
        body.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, request, Map.class);

        Map responseBody = response.getBody();
        List choices = (List) responseBody.get("choices");
        Map choice = (Map) choices.get(0);
        Map messageMap = (Map) choice.get("message");
        String result = (String) messageMap.get("content");

        return cleanResponse(result);
    }

    private String cleanResponse(String result) {
        if (result == null)
            return "";
        if (result.startsWith("```")) {
            result = result.replace("```json", "").replace("```", "").trim();
        }
        return result;
    }

    // ── 1. Generate interview questions ───────────────────────────────────────

    public List<String> generateInterviewQuestions(String field, Integer durationMinutes,
            String skills, String experienceLevel) {
        int numberOfQuestions = switch (durationMinutes) {
            case 10 -> 4;
            case 15 -> 6;
            case 20 -> 8;
            default -> 5;
        };

        String prompt = String.format("""
                Generate exactly %d technical interview questions for a %s position.

                Candidate information:
                - Skills: %s
                - Experience level: %s

                Requirements:
                - Questions must be SPECIFIC to the %s role
                - Include technical, practical, and problem-solving questions
                - Difficulty should match %s level
                - Do NOT use generic questions like "Tell me about yourself"

                Return ONLY a JSON array of strings, no extra text.
                Example: ["Question 1?", "Question 2?", "Question 3?"]
                """, numberOfQuestions, field, skills, experienceLevel, field, experienceLevel);

        String response = generateContent(prompt);
        return parseQuestionsFromJson(response);
    }

    // ── 2. Analyze answer ─────────────────────────────────────────────────────

    public Map<String, Object> analyzeAnswer(String question, String answer, String field) {
        String prompt = String.format("""
                Evaluate this answer for a %s position.

                Question: "%s"
                Answer: "%s"

                Return ONLY valid JSON with NO extra text:
                {
                  "feedback": "specific constructive feedback about this answer",
                  "correctness": "Excellent/Good/Partial/Poor",
                  "completeness": "Excellent/Good/Partial/Poor",
                  "relevance": "Excellent/Good/Partial/Poor",
                  "depth": "Excellent/Good/Partial/Poor",
                  "score": 0-10,
                  "suggestedImprovements": "specific things to improve"
                }
                """, field, question, answer);

        String response = generateContent(prompt);
        return parseAnalysisFromJson(response);
    }

    // ── 3. Overall feedback ───────────────────────────────────────────────────

    public Map<String, Object> generateOverallFeedback(String field, List<String> questions,
            List<String> answers, List<Double> scores) {
        StringBuilder qa = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            qa.append(String.format("Q%d: %s\nA%d: %s\nScore: %.1f/10\n\n",
                    i + 1, questions.get(i), i + 1,
                    answers.get(i) != null ? answers.get(i) : "No answer",
                    scores.get(i) != null ? scores.get(i) : 0.0));
        }

        String prompt = String.format("""
                Evaluate the overall interview performance for a %s role.

                Interview Q&A:
                %s

                Return ONLY valid JSON with NO extra text:
                {
                  "overallFeedback": "detailed performance summary",
                  "strongAreas": "what the candidate did well",
                  "weakAreas": "what needs improvement",
                  "studyRecommendations": "specific topics to study",
                  "accuracyScore": 0-10,
                  "overallVerdict": "Excellent/Good/Average/Needs Improvement"
                }
                """, field, qa);

        String response = generateContent(prompt);
        return parseOverallFeedbackFromJson(response);
    }

    // ── 4. Audio transcription ────────────────────────────────────────────────

    public String transcribeAudio(MultipartFile audioFile) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new RuntimeException(
                    "OpenAI API key not configured for audio transcription. Please add openai.api.key to application.properties");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(openAiApiKey);

            ByteArrayResource audioResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename() != null
                            ? audioFile.getOriginalFilename()
                            : "audio.webm";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioResource);
            body.add("model", "whisper-1");
            body.add("language", "en");

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(WHISPER_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("text").asText();

        } catch (Exception e) {
            throw new RuntimeException("Audio transcription failed: " + e.getMessage());
        }
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private List<String> parseQuestionsFromJson(String json) {
        try {
            String cleaned = json.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode array = objectMapper.readTree(cleaned);
            List<String> questions = new ArrayList<>();
            if (array.isArray()) {
                array.forEach(node -> questions.add(node.asText()));
            }
            if (questions.isEmpty()) {
                throw new RuntimeException("AI returned empty questions array");
            }
            return questions;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse questions from AI response: " + e.getMessage());
        }
    }

    private Map<String, Object> parseAnalysisFromJson(String json) {
        try {
            String cleaned = json.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);

            Map<String, Object> result = new HashMap<>();
            result.put("feedback",
                    node.has("feedback") ? node.get("feedback").asText() : "Your answer has been analyzed.");
            result.put("correctness", node.has("correctness") ? node.get("correctness").asText() : "Good");
            result.put("completeness", node.has("completeness") ? node.get("completeness").asText() : "Good");
            result.put("relevance", node.has("relevance") ? node.get("relevance").asText() : "Good");
            result.put("depth", node.has("depth") ? node.get("depth").asText() : "Good");
            result.put("score", node.has("score") ? node.get("score").asDouble() : 7.0);
            result.put("suggestedImprovements",
                    node.has("suggestedImprovements") ? node.get("suggestedImprovements").asText()
                            : "Continue practicing");
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse answer analysis from AI: " + e.getMessage());
        }
    }

    private Map<String, Object> parseOverallFeedbackFromJson(String json) {
        try {
            String cleaned = json.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);

            Map<String, Object> result = new HashMap<>();
            result.put("overallFeedback",
                    node.has("overallFeedback") ? node.get("overallFeedback").asText() : "Interview completed.");
            result.put("strongAreas", node.has("strongAreas") ? node.get("strongAreas").asText() : "Good effort");
            result.put("weakAreas", node.has("weakAreas") ? node.get("weakAreas").asText() : "Keep practicing");
            result.put("studyRecommendations",
                    node.has("studyRecommendations") ? node.get("studyRecommendations").asText()
                            : "Review core concepts");
            result.put("accuracyScore", node.has("accuracyScore") ? node.get("accuracyScore").asDouble() : 7.0);
            result.put("overallVerdict", node.has("overallVerdict") ? node.get("overallVerdict").asText() : "Average");
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse overall feedback from AI: " + e.getMessage());
        }
    }
}