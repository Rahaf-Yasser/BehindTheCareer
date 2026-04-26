package com.behindcareer.aiprep.service;

import com.google.genai.Client;
import com.behindcareer.aiprep.dto.SubmitSessionResponse;
import com.behindcareer.aiprep.dto.SubmitSessionResponse.QuestionEvaluation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    private Client client;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqKey;

    @Value("${GEMINI_API_KEY:}")
    private String geminiKey;

    private Client getClient() {
        if (client == null) {
            try {
                if (geminiKey != null && !geminiKey.isEmpty()) {
                    System.setProperty("GOOGLE_API_KEY", geminiKey);
                    client = new Client();
                }
            } catch (Exception e) {
                client = null;
            }
        }
        return client;
    }

    public String generateQuestions(String role, String level, String type, int number) {
        if ("both".equalsIgnoreCase(type)) {
            int mcqCount = number / 2;
            int codingCount = number - mcqCount;
            
            String mcqQuestions = generateQuestionsByType(role, level, "mcq", mcqCount);
            String codingQuestions = generateQuestionsByType(role, level, "coding", codingCount);
            
            return combineQuestions(mcqQuestions, codingQuestions);
        } else {
            return generateQuestionsByType(role, level, type, number);
        }
    }
    
    private String generateQuestionsByType(String role, String level, String type, int number) {
        if (number == 0) return "[]";
        String prompt = buildPromptForQuestions(role, level, type, number);
        String rawJson = callAI(prompt, type, number);
        return formatQuestionsOnly(rawJson, type);
    }
    
    private String combineQuestions(String mcqJson, String codingJson) {
        try {
            List<Map<String, Object>> mcqList = mapper.readValue(mcqJson, new TypeReference<>() {});
            List<Map<String, Object>> codingList = mapper.readValue(codingJson, new TypeReference<>() {});
            
            List<Map<String, Object>> allQuestions = new ArrayList<>();
            allQuestions.addAll(mcqList);
            allQuestions.addAll(codingList);
            
            for (int i = 0; i < allQuestions.size(); i++) {
                allQuestions.get(i).put("index", i + 1);
            }
            
            return mapper.writeValueAsString(allQuestions);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String buildPromptForQuestions(String role, String level, String type, int number) {
        if ("coding".equalsIgnoreCase(type)) {
            return String.format(
                "Generate %d REAL coding interview questions for a %s %s software engineer.\n\n" +
                "ROLE: %s\nLEVEL: %s\nTYPE: Coding Challenge\n\n" +
                "IMPORTANT: Return ONLY the questions with input/output examples.\n" +
                "DO NOT include answers, solutions, or explanations.\n\n" +
                "RETURN ONLY JSON ARRAY with this EXACT format:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"Write a function to...\",\n" +
                "    \"input\": \"Example input: [1, 2, 3]\",\n" +
                "    \"output\": \"Expected output: 6\"\n" +
                "  }\n" +
                "]\n\n" +
                "NO answers, NO solutions, NO explanations, ONLY questions.\n" +
                "Seed: %s",
                number, level, role, role, level, role, UUID.randomUUID()
            );
        } else {
            return String.format(
                "Generate %d REAL multiple-choice questions for a %s %s software engineer.\n\n" +
                "ROLE: %s\nLEVEL: %s\nTYPE: MCQ\n\n" +
                "IMPORTANT: Return ONLY the questions with options.\n" +
                "DO NOT include correct answers or explanations.\n\n" +
                "RETURN ONLY JSON ARRAY with this EXACT format:\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"What is...?\",\n" +
                "    \"options\": {\"A\": \"Option 1\", \"B\": \"Option 2\", \"C\": \"Option 3\", \"D\": \"Option 4\"}\n" +
                "  }\n" +
                "]\n\n" +
                "NO answers, NO explanations, ONLY questions and options.\n" +
                "Seed: %s",
                number, level, role, role, level, role, UUID.randomUUID()
            );
        }
    }

    private String formatQuestionsOnly(String json, String type) {
        try {
            List<Map<String, Object>> questions = mapper.readValue(json, new TypeReference<>() {});
            List<Map<String, Object>> formatted = new ArrayList<>();

            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> q = questions.get(i);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("index", i + 1);
                item.put("type", type);
                item.put("question", q.get("question"));
                
                if ("mcq".equalsIgnoreCase(type)) {
                    item.put("options", q.get("options"));
                    item.put("input", null);
                    item.put("output", null);
                } else {
                    item.put("input", q.get("input"));
                    item.put("output", q.get("output"));
                    item.put("options", null);
                }
                
                formatted.add(item);
            }
            return mapper.writeValueAsString(formatted);
        } catch (Exception e) {
            return json;
        }
    }

    // ===============================
    // EVALUATE ANSWERS - AI GENERATES CORRECT CODE SOLUTION
    // ===============================
    public String evaluateAnswers(String questionsJson, String answersJson, Long sessionId, 
                                  Boolean forceEnd, int totalQuestions, String existingEvaluationJson) {
        try {
            List<Map<String, Object>> questions = mapper.readValue(questionsJson, new TypeReference<>() {});
            List<Map<String, Object>> answers = mapper.readValue(answersJson, new TypeReference<>() {});
            
            // Get AI-powered evaluation with correct code solutions
            String aiEvaluation = getAIEvaluation(questions, answers);
            Map<String, Object> aiResult = mapper.readValue(aiEvaluation, new TypeReference<>() {});
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> evaluations = (List<Map<String, Object>>) aiResult.get("evaluations");
            
            int totalScore = 0;
            int answeredCount = 0;
            List<Map<String, Object>> evaluationList = new ArrayList<>();
            
            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> q = questions.get(i);
                Map<String, Object> eval = evaluations.get(i);
                
                String questionText = q.get("question").toString();
                String questionType = q.get("type").toString();
                String userAnswer = findAnswer(answers, i + 1);
                String correctAnswer = eval.get("correctAnswer").toString();
                String explanation = eval.get("explanation").toString();
                boolean isCorrect = (Boolean) eval.get("isCorrect");
                
                boolean isAnswered = !"Not Answered".equals(userAnswer);
                if (isAnswered) {
                    answeredCount++;
                    if (isCorrect) totalScore++;
                }
                
                String formattedExplanation = formatExplanationForUser(
                    userAnswer, correctAnswer, isAnswered, isCorrect, explanation, questionType
                );
                
                Map<String, Object> evaluation = new LinkedHashMap<>();
                evaluation.put("question", questionText);
                evaluation.put("userAnswer", userAnswer);
                evaluation.put("correctAnswer", correctAnswer);
                evaluation.put("correct", isCorrect);
                evaluation.put("score", isCorrect ? 1 : 0);
                evaluation.put("explanation", formattedExplanation);
                
                evaluationList.add(evaluation);
            }
            
            boolean completed = (forceEnd != null && forceEnd) || answeredCount >= totalQuestions;
            
            Map<String, Object> evaluationMap = new LinkedHashMap<>();
            evaluationMap.put("questionsEvaluation", evaluationList);
            evaluationMap.put("totalScore", totalScore);
            evaluationMap.put("maxScore", totalQuestions);
            evaluationMap.put("overallFeedback", generateOverallFeedback(totalScore, totalQuestions));
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("session", Map.of("id", sessionId, "completed", completed));
            response.put("evaluation", evaluationMap);
            
            return mapper.writeValueAsString(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"evaluation failed: " + e.getMessage() + "\"}";
        }
    }
    
    private String getAIEvaluation(List<Map<String, Object>> questions, List<Map<String, Object>> answers) {
        String prompt = buildEvaluationPrompt(questions, answers);
        
        try {
            Client geminiClient = getClient();
            if (geminiClient != null) {
                String result = geminiClient.models.generateContent(
                    "gemini-2.5-flash-lite",
                    prompt,
                    null
                ).text();
                String cleaned = cleanAndExtractJson(result);
                if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
                    return cleaned;
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini evaluation failed: " + e.getMessage());
        }
        
        try {
            String groqResult = callGroq(prompt);
            String cleaned = cleanAndExtractJson(groqResult);
            if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
                return cleaned;
            }
        } catch (Exception e) {
            System.err.println("Groq evaluation failed: " + e.getMessage());
        }
        
        throw new RuntimeException("Failed to get AI evaluation");
    }
    
    private String buildEvaluationPrompt(List<Map<String, Object>> questions, List<Map<String, Object>> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a technical interviewer. For each question, provide:\n");
        sb.append("1. The CORRECT CODE SOLUTION that solves the problem\n");
        sb.append("2. Whether the user's answer is correct (true/false)\n");
        sb.append("3. A DETAILED explanation of what's correct/incorrect\n\n");
        sb.append("IMPORTANT: For coding questions, the correctAnswer MUST be the actual code that solves the problem.\n\n");
        sb.append("QUESTIONS AND USER ANSWERS:\n\n");
        
        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            String userAnswer = findAnswer(answers, i + 1);
            
            sb.append("Question ").append(i + 1).append(":\n");
            sb.append("Type: ").append(q.get("type")).append("\n");
            sb.append("Question: ").append(q.get("question")).append("\n");
            
            if ("mcq".equalsIgnoreCase(q.get("type").toString())) {
                sb.append("Options: ").append(q.get("options")).append("\n");
            } else {
                sb.append("Input: ").append(q.get("input")).append("\n");
                sb.append("Output: ").append(q.get("output")).append("\n");
            }
            
            sb.append("User's Answer: ").append(userAnswer).append("\n\n");
        }
        
        sb.append("RETURN ONLY JSON with this EXACT format:\n");
        sb.append("{\n");
        sb.append("  \"evaluations\": [\n");
        sb.append("    {\n");
        sb.append("      \"correctAnswer\": \"The correct code solution or MCQ answer letter\",\n");
        sb.append("      \"isCorrect\": true or false,\n");
        sb.append("      \"explanation\": \"Detailed explanation comparing user's answer to correct solution\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        sb.append("NO extra text, ONLY JSON.\n");
        
        return sb.toString();
    }
    
    private String formatExplanationForUser(String userAnswer, String correctAnswer, 
                                            boolean isAnswered, boolean isCorrect,
                                            String explanation, String questionType) {
        if (!isAnswered) {
            return String.format("❌ Not Answered\n\n✅ Correct Solution:\n%s\n\n📖 %s", 
                correctAnswer, explanation);
        }
        
        if (isCorrect) {
            return String.format("✅ Correct!\n\nYour answer:\n%s\n\n✅ Correct Solution:\n%s\n\n📖 %s", 
                userAnswer, correctAnswer, explanation);
        } else {
            return String.format("❌ Incorrect\n\nYour answer:\n%s\n\n✅ Correct Solution:\n%s\n\n📖 %s\n\n💡 Tip: Compare your solution with the correct solution above to understand the difference.", 
                userAnswer, correctAnswer, explanation);
        }
    }
    
    private String generateOverallFeedback(int score, int total) {
        double percentage = (score * 100.0) / total;
        if (percentage >= 90) return "🏆 Excellent! Outstanding performance!";
        if (percentage >= 75) return "🎉 Very Good! Strong understanding demonstrated!";
        if (percentage >= 60) return "📚 Good! Review the correct solutions to strengthen your knowledge.";
        if (percentage >= 40) return "📖 Fair. Study the correct solutions provided above.";
        return "💪 Needs improvement. Review the correct solutions and try again.";
    }
    
    private String findAnswer(List<Map<String, Object>> answers, int index) {
        for (Map<String, Object> a : answers) {
            Object idx = a.get("questionIndex");
            if (idx != null && Integer.parseInt(idx.toString()) == index) {
                Object answer = a.get("answer");
                return answer != null ? answer.toString() : "Not Answered";
            }
        }
        return "Not Answered";
    }
    
    private String callGroq(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("temperature", 0.0);
            body.put("messages", List.of(
                Map.of("role", "system", "content", "You are a technical interviewer. Return ONLY valid JSON. NO markdown, NO extra text."),
                Map.of("role", "user", "content", prompt)
            ));

            String response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions",
                new HttpEntity<>(body, headers),
                String.class
            ).getBody();

            return extractGroqContent(response);
        } catch (Exception e) {
            return "";
        }
    }
    
    private String extractGroqContent(String raw) {
        try {
            Map<String, Object> map = mapper.readValue(raw, new TypeReference<>() {});
            List<?> choices = (List<?>) map.get("choices");
            Map<?, ?> msg = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return msg.get("content").toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    private String cleanAndExtractJson(String text) {
        if (text == null) return "";
        String cleaned = text.replace("```json", "").replace("```", "").trim();
        
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");
        if (start != -1 && end != -1 && start < end) {
            return cleaned.substring(start, end + 1);
        }
        
        start = cleaned.indexOf("[");
        end = cleaned.lastIndexOf("]");
        if (start != -1 && end != -1 && start < end) {
            return cleaned.substring(start, end + 1);
        }
        
        return cleaned;
    }
    
    private String callAI(String prompt, String type, int number) {
        try {
            Client geminiClient = getClient();
            if (geminiClient != null) {
                String result = geminiClient.models.generateContent(
                    "gemini-2.5-flash-lite",
                    prompt,
                    null
                ).text();
                String cleaned = cleanAndExtractJson(result);
                if (isValidQuestions(cleaned, number)) {
                    return cleaned;
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini failed: " + e.getMessage());
        }

        for (int i = 0; i < 3; i++) {
            try {
                String groqResult = callGroq(prompt);
                String cleaned = cleanAndExtractJson(groqResult);
                if (isValidQuestions(cleaned, number)) {
                    return cleaned;
                }
            } catch (Exception e) {
                System.err.println("Groq attempt " + (i+1) + " failed: " + e.getMessage());
            }
        }

        throw new RuntimeException("Failed to generate questions after multiple attempts");
    }
    
    private boolean isValidQuestions(String json, int expectedCount) {
        try {
            List<?> questions = mapper.readValue(json, new TypeReference<List<?>>() {});
            return questions.size() == expectedCount;
        } catch (Exception e) {
            return false;
        }
    }
}