package com.example.backend.repository;

import com.example.backend.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Integer> {
    List<InterviewSession> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    Optional<InterviewSession> findBySessionIdAndUser_UserId(Integer sessionId, Integer userId);
}