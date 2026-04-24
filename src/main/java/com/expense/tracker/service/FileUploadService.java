package com.expense.tracker.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileUploadService {

    private final String uploadDir = "uploads/profile-pictures/";

    public String uploadProfilePicture(MultipartFile file) throws IOException {
        // Papka yaratish
        Files.createDirectories(Paths.get(uploadDir));

        // Fayl nomini yaratish
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String filePath = uploadDir + fileName;

        // Faylni saqlash
        Files.copy(file.getInputStream(),
                Paths.get(filePath),
                StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/profile-pictures/" + fileName;
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "jpg";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "jpg";
    }
}