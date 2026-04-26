package com.behindcareer.aiprep.dto;

public class SubmitSessionRequest {

    private Long sessionId;
    private String answersJson;
    private Boolean forceEnd;

    // =========================
    // sessionId
    // =========================
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    // =========================
    // answersJson
    // =========================
    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    // =========================
    // forceEnd (manual session end)
    // =========================
    public Boolean getForceEnd() {
        return forceEnd;
    }

    public void setForceEnd(Boolean forceEnd) {
        this.forceEnd = forceEnd;
    }

    // =========================
    // BACKWARD COMPATIBILITY
    // =========================
    public String getUserAnswersJson() {
        return answersJson;
    }
}