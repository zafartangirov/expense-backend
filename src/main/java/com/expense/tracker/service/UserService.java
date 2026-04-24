package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.User;
import com.expense.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                                               org.springframework.web.multipart.MultipartFile file) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        // Rasm yuklash
        String pictureUrl = fileUploadService.uploadProfilePicture(file);
        user.setProfilePicture(pictureUrl);
        userRepository.save(user);

        return getProfile(email);
    }

    public void deleteProfilePicture(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));

        // Eski rasmni o'chirish
        if (user.getProfilePicture() != null) {
            try {
                String filePath = "uploads/profile-pictures/" +
                        Paths.get(user.getProfilePicture()).getFileName();
                Files.deleteIfExists(Paths.get(filePath));
            } catch (Exception e) {
                System.out.println("Fayl o'chirishda xatolik: " + e.getMessage());
            }
        }

        user.setProfilePicture(null);
        userRepository.save(user);
    }
}