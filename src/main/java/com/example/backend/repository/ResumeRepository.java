package com.example.backend.repository;

import com.example.backend.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Integer> {
    List<Resume> findByUserId(Integer userId);
    Optional<Resume> findByResumeIdAndUserId(Integer resumeId, Integer userId);
    boolean existsByUserId(Integer userId);
}