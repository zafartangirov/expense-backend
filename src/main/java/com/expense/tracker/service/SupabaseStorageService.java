package com.expense.tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        String fileName = folder + "/" + UUID.randomUUID() +
                getExtension(file.getOriginalFilename());

        WebClient client = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .defaultHeader("apikey", supabaseKey)
                .build();

        client.post()
                .uri("/storage/v1/object/" + bucket + "/" + fileName)
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null
                                ? file.getContentType()
                                : "image/jpeg"))
                .bodyValue(file.getBytes())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(bucket)) return;

        String filePath = fileUrl.substring(
                fileUrl.indexOf(bucket) + bucket.length() + 1);

        WebClient client = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .defaultHeader("apikey", supabaseKey)
                .build();

        client.delete()
                .uri("/storage/v1/object/" + bucket + "/" + filePath)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : ".jpg";
    }
}