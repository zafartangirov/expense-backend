package com.expense.tracker.controller;

import com.expense.tracker.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> body) {
        passwordResetService.sendResetCode(body.get("email"));
        return ResponseEntity.ok(Map.of(
                "message", "Kod emailga yuborildi!"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(
            @RequestBody Map<String, String> body) {
        passwordResetService.verifyCode(
                body.get("email"), body.get("code"));
        return ResponseEntity.ok(Map.of(
                "message", "Kod to'g'ri!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> body) {
        passwordResetService.resetPassword(
                body.get("email"),
                body.get("code"),
                body.get("newPassword"));
        return ResponseEntity.ok(Map.of(
                "message", "Parol muvaffaqiyatli o'zgartirildi!"));
    }
}