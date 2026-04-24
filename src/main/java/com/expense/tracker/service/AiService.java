package com.expense.tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${claude.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String categorizeExpense(String title, String description) {
        String prompt = String.format(
                "Quyidagi xarajatni kategoriyalash:\nSarlavha: %s\nTavsif: %s\n\n" +
                        "Faqat bitta kategoriya nomini qaytaring (boshqa hech narsa yozma):\n" +
                        "Food, Transport, Shopping, Health, Entertainment, Education, Housing, Other",
                title, description != null ? description : "");
        return callClaude(prompt);
    }

    public String analyzeMonthlyReport(String reportData) {
        String prompt = String.format(
                "Quyidagi oylik xarajatlar ma'lumotlari asosida qisqacha tahlil yoz " +
                        "(o'zbek tilida, 3-4 gap):\n%s\n\nAsosiy xarajat sohalari va umumiy holat haqida yoz.",
                reportData);
        return callClaude(prompt);
    }

    public String getSavingAdvice(String expenseData) {
        String prompt = String.format(
                "Foydalanuvchining xarajatlari:\n%s\n\n" +
                        "O'zbek tilida 3 ta amaliy tejash maslahatini ber " +
                        "(har biri yangi qatorda, raqam bilan). " +
                        "Faqat maslahatlarni yoz, boshqa hech narsa yozma.",
                expenseData);
        return callClaude(prompt);
    }

    public String extractFromReceipt(String base64Image) {
        String prompt = "Bu chek rasmidan quyidagi ma'lumotlarni JSON formatda chiqar:\n" +
                "{\"title\": \"do'kon/restoran nomi\", \"amount\": 0.00, " +
                "\"date\": \"YYYY-MM-DD\", \"description\": \"qisqacha tavsif\"}\n" +
                "Faqat JSON qaytart, boshqa hech narsa yozma.";
        return callClaudeWithImage(prompt, base64Image);
    }

    private boolean isApiKeyValid() {
        return apiKey != null
                && !apiKey.isBlank()
                && apiKey.startsWith("sk-ant-");
    }

    private String callClaude(String prompt) {
        if (!isApiKeyValid()) {
            System.out.println("API key yaroqsiz!");
            return "Other";
        }

        try {
            // Request body
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-opus-4-7");
            body.put("max_tokens", 1024);
            body.put("messages", List.of(message));

            String jsonBody = objectMapper.writeValueAsString(body);
            System.out.println("Request body: " + jsonBody);

            // HTTP connection
            URL url = new URL("https://api.anthropic.com/v1/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }

            // Read response
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            InputStream is = responseCode == 200
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println("Response: " + response.toString());

            if (responseCode == 200) {
                Map responseMap = objectMapper.readValue(
                        response.toString(), Map.class);
                List<Map> content = (List<Map>) responseMap.get("content");
                String result = (String) content.get(0).get("text");
                System.out.println("AI javob: " + result);
                return result.trim();
            } else {
                System.out.println("API xatolik: " + response.toString());
                return "Other";
            }

        } catch (Exception e) {
            System.out.println("AI xatolik: " + e.getMessage());
            e.printStackTrace();
            return "Other";
        }
    }

    private String callClaudeWithImage(String prompt, String base64Image) {
        if (!isApiKeyValid()) {
            return "{}";
        }

        try {
            Map<String, Object> imageSource = new HashMap<>();
            imageSource.put("type", "base64");
            imageSource.put("media_type", "image/jpeg");
            imageSource.put("data", base64Image);

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image");
            imageContent.put("source", imageSource);

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(imageContent, textContent));

            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-opus-4-7");
            body.put("max_tokens", 1024);
            body.put("messages", List.of(message));

            String jsonBody = objectMapper.writeValueAsString(body);

            URL url = new URL("https://api.anthropic.com/v1/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = responseCode == 200
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (responseCode == 200) {
                Map responseMap = objectMapper.readValue(
                        response.toString(), Map.class);
                List<Map> content = (List<Map>) responseMap.get("content");
                return (String) content.get(0).get("text");
            }
            return "{}";

        } catch (Exception e) {
            System.out.println("OCR xatolik: " + e.getMessage());
            return "{}";
        }
    }
}