package com.example.backend.repository;

import com.example.backend.model.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Integer> {
    List<InterviewQuestion> findBySession_SessionIdOrderByQuestionOrder(Integer sessionId);

    Optional<InterviewQuestion> findByQuestionIdAndSession_SessionId(Integer questionId, Integer sessionId);
}