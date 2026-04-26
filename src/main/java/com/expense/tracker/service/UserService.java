package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;
    private final SupabaseStorageService storageService;

    public UserProfileDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        int totalExpenses = expenseRepository
                .findByUserIdOrderByDateDesc(user.getId()).size();

        Double totalAmount = expenseRepository.sumByUserIdAndDateBetween(
                user.getId(),
                LocalDate.of(2000, 1, 1),
                LocalDate.now());

        return UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .totalExpenses(totalExpenses)
                .totalAmount(totalAmount != null ? totalAmount : 0)
                .profilePicture(user.getProfilePicture())
                .build();
    }

    public UserProfileDTO updateProfile(String email, UserDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        if (!user.getEmail().equals(dto.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Bu email allaqachon band!");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        userRepository.save(user);

        return getProfile(dto.getEmail());
    }

    public void changePassword(String email, ChangePasswordDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Eski parol noto'g'ri!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public UserProfileDTO uploadProfilePicture(String email,
                                               MultipartFile file) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        // Eski rasmni o'chirish
        if (user.getProfilePicture() != null) {
            storageService.deleteFile(user.getProfilePicture());
        }

        // Yangi rasmni yuklash
        String imageUrl = storageService.uploadFile(file, "avatars");
        user.setProfilePicture(imageUrl);
        userRepository.save(user);

        return getProfile(email);
    }

    public void deleteProfilePicture(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        if (user.getProfilePicture() != null) {
            storageService.deleteFile(user.getProfilePicture());
            user.setProfilePicture(null);
            userRepository.save(user);
        }
    }
}