package com.behindcareer.aiprep.dto;

public class StartSessionRequest {

    private String role;
    private String level;
    private String type;
    private int numberOfQuestions;

    public String getRole() {
        return role;
    }

    public String getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
}