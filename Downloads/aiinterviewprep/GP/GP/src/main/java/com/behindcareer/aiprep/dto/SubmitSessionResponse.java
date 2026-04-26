package com.behindcareer.aiprep.dto;

import java.util.List;

public class SubmitSessionResponse {

    private List<QuestionEvaluation> questionsEvaluation;
    private int totalScore;
    private int maxScore;
    private String overallFeedback;

    // Getters and setters
    public List<QuestionEvaluation> getQuestionsEvaluation() {
        return questionsEvaluation;
    }

    public void setQuestionsEvaluation(List<QuestionEvaluation> questionsEvaluation) {
        this.questionsEvaluation = questionsEvaluation;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    // Inner class for each question evaluation
    public static class QuestionEvaluation {
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
        private int score;
        private String explanation;

        // Getters and setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }
}