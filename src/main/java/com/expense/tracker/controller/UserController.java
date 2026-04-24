package com.expense.tracker.controller;

import com.expense.tracker.dto.*;
import com.expense.tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @Valid @RequestBody UserDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                userService.updateProfile(userDetails.getUsername(), dto));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of("message", "Parol muvaffaqiyatli o'zgartirildi!"));
    }
    @PostMapping("/profile/picture")
    public ResponseEntity<UserProfileDTO> uploadPicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(
                    userService.uploadProfilePicture(
                            userDetails.getUsername(), file));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/profile/picture")
    public ResponseEntity<Void> deletePicture(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteProfilePicture(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}