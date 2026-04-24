package com.expense.tracker.service;

import com.expense.tracker.dto.AuthResponse;
import com.expense.tracker.dto.TelegramAuthDTO;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthResponse loginWithTelegram(TelegramAuthDTO dto) throws Exception {
        // 1. Hash tekshirish
        if (!verifyTelegramHash(dto)) {
            throw new RuntimeException("Telegram ma'lumotlari noto'g'ri!");
        }

        // 2. Auth date tekshirish (1 soatdan eski bo'lmasin)
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - dto.getAuthDate() > 3600) {
            throw new RuntimeException("Telegram sessiyasi muddati o'tgan!");
        }

        // 3. User topish yoki yaratish
        String email = "telegram_" + dto.getId() + "@telegram.user";
        String fullName = dto.getFirstName() +
                (dto.getLastName() != null ? " " + dto.getLastName() : "");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setEmailVerified(true); // ✅ Telegram tasdiqlangan
                    newUser.setVerificationToken(null);
                    return userRepository.save(newUser);
                });

        user.setFullName(fullName);
        user.setEmailVerified(true); // ✅
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    private boolean verifyTelegramHash(TelegramAuthDTO dto) throws Exception {
        // Bot token dan secret key yasash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] secretKey = digest.digest(
                botToken.getBytes(StandardCharsets.UTF_8));

        // Data string yasash
        Map<String, String> dataMap = new TreeMap<>();
        dataMap.put("auth_date", String.valueOf(dto.getAuthDate()));
        dataMap.put("first_name", dto.getFirstName());
        if (dto.getLastName() != null)
            dataMap.put("last_name", dto.getLastName());
        if (dto.getUsername() != null)
            dataMap.put("username", dto.getUsername());
        if (dto.getPhotoUrl() != null)
            dataMap.put("photo_url", dto.getPhotoUrl());
        dataMap.put("id", String.valueOf(dto.getId()));

        StringBuilder dataString = new StringBuilder();
        dataMap.forEach((k, v) ->
                dataString.append(k).append("=").append(v).append("\n"));
        String dataCheck = dataString.toString().trim();

        // HMAC-SHA256 hisoblash
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] hmac = mac.doFinal(
                dataCheck.getBytes(StandardCharsets.UTF_8));

        // Hex ga o'girish
        StringBuilder hexString = new StringBuilder();
        for (byte b : hmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString().equals(dto.getHash());
    }
}