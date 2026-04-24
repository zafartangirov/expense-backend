package com.expense.tracker.service;

import com.expense.tracker.dto.AuthResponse;
import com.expense.tracker.dto.GitHubAuthDTO;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GitHubAuthService {

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final WebClient webClient = WebClient.builder().build();

    public AuthResponse loginWithGitHub(GitHubAuthDTO dto) {
        // 1. Code → Access Token
        String accessToken = getAccessToken(dto.getCode());

        // 2. Access Token → User info
        Map userInfo = getUserInfo(accessToken);

        // 3. Email olish
        String email = (String) userInfo.get("email");
        if (email == null || email.isEmpty()) {
            Integer id = (Integer) userInfo.get("id");
            email = "github_" + id + "@github.user";
        }

        // 4. Ism olish
        String fullName = (String) userInfo.get("name");
        String login = (String) userInfo.get("login");
        if (fullName == null || fullName.isEmpty()) {
            fullName = login;
        }

        final String finalEmail = email;
        final String finalName = fullName;

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setFullName(finalName);
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setEmailVerified(true); // ✅ GitHub email tasdiqlangan
                    newUser.setVerificationToken(null);
                    return userRepository.save(newUser);
                });

        user.setFullName(finalName);
        user.setEmailVerified(true); // ✅
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFullName());
    }

    private String getAccessToken(String code) {
        Map response = webClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("GitHub token olishda xatolik!");
        }

        return (String) response.get("access_token");
    }

    private Map getUserInfo(String accessToken) {
        Map userInfo = webClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userInfo == null) {
            throw new RuntimeException("GitHub user ma'lumotlarini olishda xatolik!");
        }

        return userInfo;
    }
}