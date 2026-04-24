package com.expense.tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.builder().build();

    // Kurslarni olish
    public Map<String, Object> getRates(String baseCurrency) {
        try {
            Map response = webClient.get()
                    .uri(apiUrl + "/" + apiKey + "/latest/" + baseCurrency)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Kurslarni olishda xatolik!");
            }

            Map<String, Double> rates = (Map<String, Double>) response.get("conversion_rates");

            // Faqat kerakli valyutalarni qaytarish
            Map<String, Object> result = new HashMap<>();
            result.put("base", baseCurrency);
            result.put("UZS", rates.get("UZS"));
            result.put("USD", rates.get("USD"));
            result.put("EUR", rates.get("EUR"));
            result.put("RUB", rates.get("RUB"));
            result.put("GBP", rates.get("GBP"));
            result.put("KZT", rates.get("KZT"));
            result.put("TRY", rates.get("TRY"));
            result.put("AED", rates.get("AED"));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Valyuta kurslari olishda xatolik: " + e.getMessage());
        }
    }

    // Konvertatsiya qilish
    public Map<String, Object> convert(String from, String to, Double amount) {
        try {
            Map response = webClient.get()
                    .uri(apiUrl + "/" + apiKey + "/pair/" + from + "/" + to + "/" + amount)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Konvertatsiya xatolik!");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("from", from);
            result.put("to", to);
            result.put("amount", amount);
            result.put("result", response.get("conversion_result"));
            result.put("rate", response.get("conversion_rate"));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Konvertatsiya xatolik: " + e.getMessage());
        }
    }
}