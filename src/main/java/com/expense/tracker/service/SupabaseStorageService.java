package com.expense.tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private WebClient client;

    @PostConstruct
    public void init() {
        System.out.println("Supabase URL: " + supabaseUrl);
        System.out.println("Supabase Bucket: " + bucket);
        this.client = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("x-upsert", "true")
                .build();
    }

    public String uploadFile(MultipartFile file, String folder) throws Exception {
        String fileName = folder + "/" + UUID.randomUUID() +
                getExtension(file.getOriginalFilename());

        System.out.println("Uploading to Supabase: " + fileName);

        String response = client.post()
                .uri("/storage/v1/object/" + bucket + "/" + fileName)
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null
                                ? file.getContentType()
                                : "image/jpeg"))
                .bodyValue(file.getBytes())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Supabase response: " + response);

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(bucket)) return;
        try {
            String filePath = fileUrl.substring(
                    fileUrl.indexOf(bucket) + bucket.length() + 1);
            client.delete()
                    .uri("/storage/v1/object/" + bucket + "/" + filePath)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null) return ".jpg";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : ".jpg";
    }
}