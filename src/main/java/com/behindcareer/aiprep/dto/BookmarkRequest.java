package com.behindcareer.aiprep.dto;

public class BookmarkRequest {

    private Long sessionId;
    private Integer questionNumber;
    private Boolean bookmarkAllPrevious;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public Boolean getBookmarkAllPrevious() {
        return bookmarkAllPrevious;
    }

    public void setBookmarkAllPrevious(Boolean bookmarkAllPrevious) {
        this.bookmarkAllPrevious = bookmarkAllPrevious;
    }

    public Integer getQuestionId() {
        return questionNumber;
    }
}