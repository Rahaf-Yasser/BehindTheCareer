package com.example.backend.controller;

import com.example.backend.dto.ProfileRequest;
import com.example.backend.dto.ProfileResponse;
import com.example.backend.security.UserDetailsImpl;
import com.example.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // GET /api/profile
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getUserId()));
    }

    // POST /api/profile (first-time setup)
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.createProfile(userDetails.getUserId(), request));
    }

    // PUT /api/profile (edit profile)
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getUserId(), request));
    }

    // DELETE /api/profile
    @DeleteMapping
    public ResponseEntity<String> deleteProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        profileService.deleteProfile(userDetails.getUserId());
        return ResponseEntity.ok("Profile deleted successfully");
    }
}