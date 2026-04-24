package com.expense.tracker.service;

import com.expense.tracker.dto.AuthResponse;
import com.expense.tracker.dto.GoogleAuthDTO;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String clientId;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthResponse loginWithGoogle(GoogleAuthDTO dto) throws Exception {
        // 1. Google token tekshirish
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(dto.getCredential());
        if (idToken == null) {
            throw new RuntimeException("Google token noto'g'ri!");
        }

        // 2. Foydalanuvchi ma'lumotlari
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        if (fullName == null || fullName.isEmpty()) {
            fullName = email;
        }

        final String finalEmail = email;
        final String finalName = fullName;

        // 3. User topish yoki yaratish
        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setFullName(finalName);
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setEmailVerified(true);
                    newUser.setVerificationToken(null);
                    return userRepository.save(newUser);
                });

        // 4. Mavjud userni yangilash
        user.setFullName(finalName);
        user.setEmailVerified(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }
}