package com.expense.tracker.service;

import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        System.out.println("Yaratilgan token: " + token);
        System.out.println("User email: " + user.getEmail());

        user.setVerificationToken(token);
        User savedUser = userRepository.saveAndFlush(user);

        System.out.println("Saqlangan token: " + savedUser.getVerificationToken());

        emailService.sendVerificationEmail(
                user.getEmail(), user.getFullName(), token);
    }

    @Transactional
    public void verifyEmail(String token) {
        System.out.println("Kelgan token: " + token);

        User user = userRepository.findByVerificationToken(token)
                .orElseGet(() -> {
                    // Token null bo'lsa — allaqachon tasdiqlangan bo'lishi mumkin
                    System.out.println("Token topilmadi — allaqachon tasdiqlangan!");
                    return null;
                });

        if (user == null) {
            throw new RuntimeException("Email allaqachon tasdiqlangan!");
        }

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email allaqachon tasdiqlangan!");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.saveAndFlush(user);

        System.out.println("Email tasdiqlandi: " + user.getEmail());
    }
}