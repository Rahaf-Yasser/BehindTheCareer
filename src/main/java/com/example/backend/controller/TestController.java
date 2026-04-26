package com.example.backend.controller;

import com.example.backend.security.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Slf4j
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<String> publicTest() {
        log.info("Public test endpoint accessed");
        return ResponseEntity.ok("Public endpoint works! No auth needed.");
    }

    @GetMapping("/secure-test")
    public ResponseEntity<String> secureTest(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("Secure test endpoint accessed by user: {}", userDetails.getUsername());
        return ResponseEntity
                .ok("Authenticated as: " + userDetails.getUsername() + " with ID: " + userDetails.getUserId());
    }
}