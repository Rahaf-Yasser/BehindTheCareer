package com.example.backend.repository;

import com.example.backend.model.AIResumeAnalyzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIResumeAnalyzerRepository extends JpaRepository<AIResumeAnalyzer, Integer> {
    List<AIResumeAnalyzer> findByResumeIdOrderByAnalyzedAtDesc(Integer resumeId);
}