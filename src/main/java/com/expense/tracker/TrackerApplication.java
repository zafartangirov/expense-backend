package com.expense.tracker;

import com.expense.tracker.entity.Category;
import com.expense.tracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class TrackerApplication {

    private final CategoryRepository categoryRepository;

    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedCategories() {
        return args -> {
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        Category.builder().name("Food").icon("🍔").color("#EF4444").build(),
                        Category.builder().name("Transport").icon("🚗").color("#3B82F6").build(),
                        Category.builder().name("Shopping").icon("🛍️").color("#8B5CF6").build(),
                        Category.builder().name("Health").icon("💊").color("#10B981").build(),
                        Category.builder().name("Entertainment").icon("🎬").color("#F59E0B").build(),
                        Category.builder().name("Education").icon("📚").color("#6366F1").build(),
                        Category.builder().name("Housing").icon("🏠").color("#14B8A6").build(),
                        Category.builder().name("Other").icon("💰").color("#6B7280").build()
                ));
                System.out.println("Kategoriyalar yuklandi!");
            }
        };
    }
}