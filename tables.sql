-- ── ChatGPT Interaction (create first, others depend on it) ──
CREATE TABLE IF NOT EXISTS chatgpt_interaction (
    interaction_id INT PRIMARY KEY AUTO_INCREMENT,
    context_type VARCHAR(255),
    prompt TEXT,
    response TEXT,
    related_resume_id INT,
    related_session_id INT,
    related_practice_session_id INT,
    created_at DATETIME DEFAULT NOW()
);

-- ── Interview Session ──
CREATE TABLE IF NOT EXISTS interview_session (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    profile_id INT,
    type VARCHAR(255),
    video_path VARCHAR(255),
    audio_path VARCHAR(255),
    feedback TEXT,
    soft_skills_metrics JSON,
    chat_gpt_interaction_id INT,
    created_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    FOREIGN KEY (chat_gpt_interaction_id) REFERENCES chatgpt_interaction(interaction_id)
);

-- ── AI Resume Analyzer ──
CREATE TABLE IF NOT EXISTS ai_resume_analyzer (
    analysis_id INT PRIMARY KEY AUTO_INCREMENT,
    resume_id INT,
    chat_gpt_interaction_id INT,
    feedback_text TEXT,
    improvement_suggestions TEXT,
    analyzed_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (resume_id) REFERENCES resume(resume_id),
    FOREIGN KEY (chat_gpt_interaction_id) REFERENCES chatgpt_interaction(interaction_id)
);

-- ── Video Analysis ──
CREATE TABLE IF NOT EXISTS video_analysis (
    analysis_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT,
    audio_emotion JSON,
    facial_expression JSON,
    soft_skills_metrics JSON,
    analyzed_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (session_id) REFERENCES interview_session(session_id)
);

-- ── Practice Session ──
CREATE TABLE IF NOT EXISTS practice_session (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    profile_id INT,
    topic VARCHAR(255),
    created_at DATETIME DEFAULT NOW(),
    chat_gpt_interaction_id INT,
    FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    FOREIGN KEY (chat_gpt_interaction_id) REFERENCES chatgpt_interaction(interaction_id)
);

-- ── Question Bank ──
CREATE TABLE IF NOT EXISTS question_bank (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT,
    category VARCHAR(255),
    difficulty VARCHAR(255),
    company_tag VARCHAR(255),
    text TEXT,
    FOREIGN KEY (session_id) REFERENCES practice_session(session_id)
);