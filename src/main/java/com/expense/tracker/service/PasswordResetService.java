package com.expense.tracker.service;

import com.expense.tracker.entity.PasswordResetToken;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.PasswordResetTokenRepository;
import com.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // 1. Email yuborish
    @Transactional
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Bu email bilan foydalanuvchi topilmadi!"));

        // Eski tokenlarni o'chirish
        tokenRepository.deleteByEmail(email);

        // 6 raqamli kod yaratish
        String code = String.format("%06d",
                new Random().nextInt(999999));

        // Token saqlash
        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .token(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        tokenRepository.save(token);

        // Email yuborish
        emailService.sendPasswordResetEmail(email, user.getFullName(), code);
    }

    // 2. Kodni tekshirish
    public void verifyCode(String email, String code) {
        PasswordResetToken token = tokenRepository.findByToken(code)
                .orElseThrow(() -> new RuntimeException("Kod noto'g'ri!"));

        if (!token.getEmail().equals(email)) {
            throw new RuntimeException("Kod noto'g'ri!");
        }

        if (token.isUsed()) {
            throw new RuntimeException("Bu kod allaqachon ishlatilgan!");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Kod muddati o'tgan! Yangi kod oling.");
        }
    }

    // 3. Yangi parol o'rnatish
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyCode(email, code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Tokenni ishlatilgan deb belgilash
        PasswordResetToken token = tokenRepository.findByToken(code).get();
        token.setUsed(true);
        tokenRepository.save(token);
    }
}