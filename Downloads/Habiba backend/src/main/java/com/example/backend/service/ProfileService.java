package com.example.backend.service;

import com.example.backend.dto.ProfileRequest;
import com.example.backend.dto.ProfileResponse;
import com.example.backend.model.Profile;
import com.example.backend.model.User;
import com.example.backend.repository.ProfileRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // GET profile of logged-in user
    public ProfileResponse getProfile(Integer userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return toResponse(profile);
    }

    // POST - create profile for the first time
    public ProfileResponse createProfile(Integer userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (profileRepository.findByUser_UserId(userId).isPresent()) {
            throw new RuntimeException("Profile already exists. Use PUT to update.");
        }

        Profile profile = Profile.builder()
                .user(user)
                .bio(request.getBio())
                .field(toCommaSeparated(request.getFields()))
                .skills(toCommaSeparated(request.getSkills()))
                .experienceLevel(request.getExperienceLevel())
                .profilePicture(request.getProfilePicture())
                .build();

        return toResponse(profileRepository.save(profile));
    }

    // PUT - update existing profile
    public ProfileResponse updateProfile(Integer userId, ProfileRequest request) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found. Use POST to create one first."));

        if (request.getBio() != null)
            profile.setBio(request.getBio());
        if (request.getFields() != null)
            profile.setField(toCommaSeparated(request.getFields()));
        if (request.getSkills() != null)
            profile.setSkills(toCommaSeparated(request.getSkills()));
        if (request.getExperienceLevel() != null)
            profile.setExperienceLevel(request.getExperienceLevel());
        if (request.getProfilePicture() != null)
            profile.setProfilePicture(request.getProfilePicture());

        return toResponse(profileRepository.save(profile));
    }

    // DELETE profile
    public void deleteProfile(Integer userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profileRepository.delete(profile);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String toCommaSeparated(List<String> list) {
        if (list == null || list.isEmpty())
            return null;
        return String.join(",", list);
    }

    private List<String> toList(String value) {
        if (value == null || value.isBlank())
            return List.of();
        return Arrays.asList(value.split(","));
    }

    private ProfileResponse toResponse(Profile profile) {
        ProfileResponse response = ProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .fullName(profile.getUser().getFullName())
                .email(profile.getUser().getEmail())
                .bio(profile.getBio())
                .fields(toList(profile.getField()))
                .skills(toList(profile.getSkills()))
                .experienceLevel(profile.getExperienceLevel())
                .profilePicture(profile.getProfilePicture())
                .build();

        // Set the formatted date
        if (profile.getUpdatedAt() != null) {
            response.setUpdatedAt(profile.getUpdatedAt());
        }

        return response;
    }
}