package com.expense.tracker.controller;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Autowired
    private TelegramAuthService telegramAuthService;

    @PostMapping("/telegram")
    public ResponseEntity<AuthResponse> telegramLogin(
            @RequestBody TelegramAuthDTO dto) {
        try {
            return ResponseEntity.ok(telegramAuthService.loginWithTelegram(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @RequestBody GoogleAuthDTO dto) {
        try {
            return ResponseEntity.ok(googleAuthService.loginWithGoogle(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    @Autowired
    private GitHubAuthService gitHubAuthService;

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> githubLogin(
            @RequestBody GitHubAuthDTO dto) {
        try {
            return ResponseEntity.ok(gitHubAuthService.loginWithGitHub(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GitHub callback — code ni qabul qilib frontendga yuboradi
    @GetMapping("/github/callback")
    public void githubCallback(
            @RequestParam String code,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        response.sendRedirect("https://expensetrackerfrontend-tau.vercel.app/github/callback?code=" + code);
    }


    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String token) {
        try {
            System.out.println("Token keldi: " + token);
            verificationService.verifyEmail(token);
            return ResponseEntity.ok(Map.of(
                    "message", "Email muvaffaqiyatli tasdiqlandi!"));
        } catch (Exception e) {
            System.out.println("Verify xatolik: " + e.getMessage());
            if (e.getMessage().contains("allaqachon")) {
                // Allaqachon tasdiqlangan — frontendga muvaffaqiyat qaytaramiz
                return ResponseEntity.ok(Map.of(
                        "message", "Email allaqachon tasdiqlangan!"));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findByEmail(body.get("email"))
                    .orElseThrow(() -> new RuntimeException(
                            "Foydalanuvchi topilmadi!"));
            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email allaqachon tasdiqlangan!"));
            }
            verificationService.sendVerificationEmail(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Tasdiqlash emaili qayta yuborildi!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}