package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email allaqachon ro'yxatdan o'tgan!");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        System.out.println("Yaratilgan token: " + token);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .verificationToken(token)
                .build();

        User savedUser = userRepository.saveAndFlush(user);
        System.out.println("Saqlangan token: " + savedUser.getVerificationToken());
        System.out.println("Saqlangan verified: " + savedUser.isEmailVerified());

        // Emailni to'g'ridan-to'g'ri yuboramiz
        emailService.sendVerificationEmail(
                savedUser.getEmail(), savedUser.getFullName(), token);

        return new AuthResponse(null, savedUser.getEmail(), savedUser.getFullName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Bunday foydalanuvchi mavjud emas!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Parol noto'g'ri!");
        }

        if (!user.isEmailVerified()) {
            // Social login emaillarini o'tkazib yuborish
            if (!user.getEmail().contains("@github.user") &&
                    !user.getEmail().contains("@telegram.user")) {
                throw new RuntimeException("EMAIL_NOT_VERIFIED");
            }
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }
}