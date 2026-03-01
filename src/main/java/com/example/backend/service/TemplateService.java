package com.example.backend.service;

import com.example.backend.model.Template;
import com.example.backend.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public List<Template> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    public Template getTemplateById(Integer templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
    }
}