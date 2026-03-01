package com.example.backend.repository;

import com.example.backend.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {
    List<Template> findByIsActiveTrue();
}