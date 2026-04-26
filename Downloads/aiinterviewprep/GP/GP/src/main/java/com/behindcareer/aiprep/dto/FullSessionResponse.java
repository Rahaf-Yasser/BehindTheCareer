package com.behindcareer.aiprep.dto;

import com.behindcareer.aiprep.entity.PracticeSession;

public class FullSessionResponse {

    private PracticeSession session;
    private SubmitSessionResponse evaluation;

    public FullSessionResponse() {}

    public FullSessionResponse(PracticeSession session, SubmitSessionResponse evaluation) {
        this.session = session;
        this.evaluation = evaluation;
    }

    public PracticeSession getSession() { return session; }
    public void setSession(PracticeSession session) { this.session = session; }

    public SubmitSessionResponse getEvaluation() { return evaluation; }
    public void setEvaluation(SubmitSessionResponse evaluation) { this.evaluation = evaluation; }
}