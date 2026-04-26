package com.behindcareer.aiprep.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "practice_session")
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private String type;

    @Column(name = "number_of_questions", nullable = false)
    private int numberOfQuestions;

    @Column(name = "generated_questions_json", columnDefinition = "TEXT")
    private String generatedQuestionsJson;

    @Column(name = "user_answers_json", columnDefinition = "TEXT")
    private String userAnswersJson;

    @Column(name = "evaluation_json", columnDefinition = "TEXT")
    private String evaluationJson;

    @Column(name = "bookmarked_questions", columnDefinition = "TEXT")
    private String bookmarkedQuestions;

    @Column(nullable = false)
    private boolean completed;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(int numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }

    public String getGeneratedQuestionsJson() { return generatedQuestionsJson; }
    public void setGeneratedQuestionsJson(String generatedQuestionsJson) { this.generatedQuestionsJson = generatedQuestionsJson; }

    public String getUserAnswersJson() { return userAnswersJson; }
    public void setUserAnswersJson(String userAnswersJson) { this.userAnswersJson = userAnswersJson; }

    public String getEvaluationJson() { return evaluationJson; }
    public void setEvaluationJson(String evaluationJson) { this.evaluationJson = evaluationJson; }

    public String getBookmarkedQuestions() { return bookmarkedQuestions; }
    public void setBookmarkedQuestions(String bookmarkedQuestions) { this.bookmarkedQuestions = bookmarkedQuestions; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}