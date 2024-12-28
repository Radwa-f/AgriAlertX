package com.example.demo.controller;

import com.example.demo.repository.AppUserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.service.AppUserService;
import com.example.demo.utils.profile.LocationUpdateRequest;
import com.example.demo.utils.profile.ProfileUpdateRequest;
import com.example.demo.model.AppUser;
import com.example.demo.utils.registration.token.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/user")
@AllArgsConstructor
@Validated
public class AppUserController {

    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final JwtUtils jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<AppUser> getProfile() {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Fetch the user's details
        Optional<AppUser> appUserOptional = appUserRepository.findByEmail(email);

        return appUserOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    /**
     * Update the current user's profile information.
     *
     * @param profileUpdateRequest object containing the updated profile information
     * @return the updated profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Update user profile
        Optional<AppUser> appUserOptional = appUserRepository.findByEmail(email);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();

            // Update fields based on the request
            appUser.setFirstName(profileUpdateRequest.getFirstName());
            appUser.setLastName(profileUpdateRequest.getLastName());
            appUser.setPhoneNumber(profileUpdateRequest.getPhoneNumber());

            // Update the crops field with the new crop name(s)
            if (profileUpdateRequest.getCrops() != null) {
                appUser.setCrops(profileUpdateRequest.getCrops());
            }

            // Save updated user
            appUserRepository.save(appUser);

            // Generate a new JWT token with the updated user information
            CustomUserDetails customUserDetails = new CustomUserDetails(appUser);
            String newToken = jwtUtil.generateToken(customUserDetails);

            // Prepare the response map containing the updated user and the new token
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Profile updated successfully");
            responseMap.put("newToken", newToken);

            return ResponseEntity.ok(responseMap);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/location")
    public ResponseEntity<String> updateLocation(@Valid @RequestBody LocationUpdateRequest locationUpdateRequest) {
        // Get the currently authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Update the location through the service method
        String response = appUserService.updateLocation(email, locationUpdateRequest.getLatitude(), locationUpdateRequest.getLongitude());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<AppUser> appUserOptional = appUserRepository.findByEmail(email);
        if (appUserOptional.isPresent()) {
            AppUser user = appUserOptional.get();

            // First, delete dependent tokens
            confirmationTokenRepository.deleteByAppUser(user);

            // Then, delete the user
            appUserRepository.delete(user);
            return ResponseEntity.ok("Account deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
