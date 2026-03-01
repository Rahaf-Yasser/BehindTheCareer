package com.example.backend.controller;

import com.example.backend.model.Template;
import com.example.backend.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    // GET /api/templates
    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllActiveTemplates());
    }

    // GET /api/templates/{templateId}
    @GetMapping("/{templateId}")
    public ResponseEntity<Template> getTemplate(@PathVariable Integer templateId) {
        return ResponseEntity.ok(templateService.getTemplateById(templateId));
    }
}