package com.example.backend.service;

import com.example.backend.model.AIResumeAnalyzer;
import com.example.backend.model.Resume;
import com.example.backend.repository.AIResumeAnalyzerRepository;
import com.example.backend.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeAnalyzerService {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AIResumeAnalyzerRepository analyzerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResumeRepository resumeRepository;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama3-70b-8192}")
    private String groqModel;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    // Simple cache to avoid repeated API calls for the same resume
    private final Map<Integer, CachedAnalysis> analysisCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 3600000; // 1 hour

    // ─────────────────────────────────────────────
    // MAIN METHOD — analyze a resume by ID
    // ─────────────────────────────────────────────
    public JsonNode analyzeResume(Integer resumeId) throws Exception {
        // Check cache first
        CachedAnalysis cached = analysisCache.get(resumeId);
        if (cached != null && !cached.isExpired()) {
            System.out.println("✅ Returning cached analysis for resume " + resumeId);
            return objectMapper.readTree(cached.analysis);
        }

        // 1. Fetch resume
        Resume resume = resumeService.getRawResume(resumeId);
        JsonNode content = resume.getContent();

        if (content == null) {
            throw new RuntimeException("Resume has no content to analyze.");
        }

        // 2. Build prompt (same as before)
        String prompt = buildPrompt(content);

        // 3. Call Groq API
        String groqResponse = callGroq(prompt);

        // 4. Parse response
        JsonNode analysis = parseAnalysis(groqResponse);

        // 5. Save to database
        saveAnalysis(resumeId, prompt, groqResponse, analysis);

        // 6. Cache the result
        analysisCache.put(resumeId, new CachedAnalysis(analysis.toString()));

        return analysis;
    }

    // ─────────────────────────────────────────────
    // GET previous analyses for a resume
    // ─────────────────────────────────────────────
    public List<AIResumeAnalyzer> getAnalysesForResume(Integer resumeId) {
        return analyzerRepository.findByResumeIdOrderByAnalyzedAtDesc(resumeId);
    }

    // ─────────────────────────────────────────────
    // BUILD PROMPT (EXACTLY THE SAME AS BEFORE)
    // ─────────────────────────────────────────────
    private String buildPrompt(JsonNode content) {
        JsonNode pi         = content.path("personalInfo");
        JsonNode experience = content.path("experience");
        JsonNode education  = content.path("education");
        JsonNode skills     = content.path("skills");
        String   summary    = content.path("summary").asText("");

        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional resume reviewer. Analyze the following resume and return a JSON response ONLY — no extra text, no markdown, no code blocks.\n\n");
        sb.append("The JSON must follow this exact structure:\n");
        sb.append("{\n");
        sb.append("  \"scores\": {\n");
        sb.append("    \"formatting\": <0-100>,\n");
        sb.append("    \"clarity\": <0-100>,\n");
        sb.append("    \"keywordRelevance\": <0-100>,\n");
        sb.append("    \"overall\": <0-100>\n");
        sb.append("  },\n");
        sb.append("  \"strengths\": [\"...\", \"...\"],\n");
        sb.append("  \"improvements\": [\"...\", \"...\"],\n");
        sb.append("  \"detailedFeedback\": \"...\"\n");
        sb.append("}\n\n");
        sb.append("Here is the resume to analyze:\n\n");

        // Personal Info
        sb.append("NAME: ").append(pi.path("fullName").asText("")).append("\n");
        sb.append("EMAIL: ").append(pi.path("email").asText("")).append("\n");
        sb.append("PHONE: ").append(pi.path("phone").asText("")).append("\n");
        sb.append("LOCATION: ").append(pi.path("address").asText("")).append("\n\n");

        // Summary
        if (!summary.isBlank()) {
            sb.append("SUMMARY:\n").append(summary).append("\n\n");
        }

        // Experience
        if (experience.isArray() && !experience.isEmpty()) {
            sb.append("EXPERIENCE:\n");
            for (JsonNode exp : experience) {
                String role = exp.path("jobTitle").asText("");
                if (role.isBlank()) role = exp.path("role").asText("");
                
                sb.append("- ").append(role).append(" at ")
                        .append(exp.path("company").asText("")).append("\n");
                sb.append("  ").append(exp.path("startDate").asText("")).append(" to ")
                        .append(exp.path("current").asBoolean(false) ? "Present" : exp.path("endDate").asText(""))
                        .append("\n");
                String desc = exp.path("description").asText("");
                if (!desc.isBlank()) sb.append("  ").append(desc).append("\n");
            }
            sb.append("\n");
        }

        // Education
        if (education.isArray() && !education.isEmpty()) {
            sb.append("EDUCATION:\n");
            for (JsonNode edu : education) {
                String degree = edu.path("degree").asText("");
                String field = edu.path("fieldOfStudy").asText("");
                if (field.isBlank()) field = edu.path("field").asText("");
                
                sb.append("- ").append(degree);
                if (!field.isBlank()) sb.append(" in ").append(field);
                sb.append(" at ").append(edu.path("institution").asText(""));
                sb.append(" (").append(edu.path("endDate").asText("")).append(")\n");
            }
            sb.append("\n");
        }

        // Skills
        if (skills.isArray() && !skills.isEmpty()) {
            sb.append("SKILLS: ");
            StringBuilder skillList = new StringBuilder();
            for (JsonNode skill : skills) {
                if (!skillList.isEmpty()) skillList.append(", ");
                String skillName = skill.path("name").asText("");
                if (skillName.isBlank()) skillName = skill.asText();
                skillList.append(skillName);
            }
            sb.append(skillList).append("\n");
        }

        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // CALL GROQ API (REPLACES GEMINI)
    // ─────────────────────────────────────────────
    private String callGroq(String prompt) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
    
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are a professional resume reviewer. Always respond with valid JSON only.");
    
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
    
        JsonArray messages = new JsonArray();
        messages.add(systemMessage);
        messages.add(userMessage);
    
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", groqModel);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 4000);  
        requestBody.addProperty("top_p", 0.95);
        requestBody.addProperty("frequency_penalty", 0);
        requestBody.addProperty("presence_penalty", 0);
    
        System.out.println("📤 Sending analysis request to Groq using model: " + groqModel);
    
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
        if (response.statusCode() != 200) {
            System.err.println("Groq API Error Response: " + response.body());
            throw new RuntimeException("Groq API error: " + response.statusCode() + " — " + response.body());
        }
    
        System.out.println("✅ Groq API call successful, response length: " + response.body().length());
        return response.body();
    }

    // ─────────────────────────────────────────────
    // PARSE GROQ RESPONSE → structured JSON
    // ─────────────────────────────────────────────
    private JsonNode parseAnalysis(String groqResponse) throws Exception {
        JsonObject groqJson = JsonParser.parseString(groqResponse).getAsJsonObject();

        // Extract the text content from Groq's response structure (OpenAI-compatible)
        String text = groqJson
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        // Clean up in case model wraps in markdown
        text = text.strip();
        if (text.startsWith("```json")) text = text.substring(7);
        if (text.startsWith("```"))     text = text.substring(3);
        if (text.endsWith("```"))       text = text.substring(0, text.length() - 3);
        text = text.strip();

        // Validate the JSON has the expected structure
        JsonNode analysis = objectMapper.readTree(text);
        
        // If any required fields are missing, fill with defaults
        if (!analysis.has("scores")) {
            System.err.println("⚠️ Groq response missing 'scores', using defaults");
            return createDefaultAnalysis();
        }

        return analysis;
    }

    // ─────────────────────────────────────────────
    // FALLBACK: Create default analysis if API fails
    // ─────────────────────────────────────────────
    private JsonNode createDefaultAnalysis() throws Exception {
        String defaultJson = """
            {
                "scores": {
                    "formatting": 70,
                    "clarity": 70,
                    "keywordRelevance": 70,
                    "overall": 70
                },
                "strengths": ["AI analysis temporarily unavailable. Please try again later."],
                "improvements": ["Connect to AI service for detailed feedback."],
                "detailedFeedback": "Unable to analyze resume at this time. Please check your internet connection and try again."
            }
            """;
        return objectMapper.readTree(defaultJson);
    }

    // ─────────────────────────────────────────────
    // SAVE ANALYSIS TO DATABASE (EXACTLY THE SAME)
    // ─────────────────────────────────────────────
    private void saveAnalysis(Integer resumeId, String prompt, String rawResponse, JsonNode analysis) {
        // Save to ai_resume_analyzer table
        AIResumeAnalyzer record = new AIResumeAnalyzer();
        record.setResumeId(resumeId);
        record.setFeedbackText(analysis.path("detailedFeedback").asText(""));
        record.setImprovementSuggestions(analysis.path("improvements").toString());
        record.setAnalyzedAt(LocalDateTime.now());
        analyzerRepository.save(record);

        // Also save feedback to resume's aiFeedback field
        Resume resume = resumeService.getRawResume(resumeId);
        resume.setAiFeedback(analysis.toString());
        resumeRepository.save(resume);
        
        System.out.println("💾 Analysis saved for resume " + resumeId);
    }

    // ─────────────────────────────────────────────
    // Clear cache for a specific resume (useful when resume is updated)
    // ─────────────────────────────────────────────
    public void clearCache(Integer resumeId) {
        analysisCache.remove(resumeId);
        System.out.println("🗑️ Cache cleared for resume " + resumeId);
    }

    // ─────────────────────────────────────────────
    // Inner class for caching
    // ─────────────────────────────────────────────
    private static class CachedAnalysis {
        final String analysis;
        final long timestamp;
        
        CachedAnalysis(String analysis) {
            this.analysis = analysis;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}