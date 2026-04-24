package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.*;
import com.expense.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AiService aiService;
    private final CurrencyService currencyService;

    public ExpenseResponse create(ExpenseDTO dto, String email) {
        User user = getUser(email);

        // AI bilan avtomatik kategoriyalash
        String categoryName = aiService.categorizeExpense(
                dto.getTitle(), dto.getDescription());

        Category category = categoryRepository.findByName(categoryName.trim())
                .orElseGet(() -> categoryRepository.findByName("Other")
                        .orElseGet(() -> categoryRepository.save(
                                Category.builder()
                                        .name(categoryName.trim())
                                        .icon("💰")
                                        .color("#6B7280")
                                        .build())));

        // Valyuta konvertatsiya
        BigDecimal finalAmount = dto.getAmount();
        Double originalAmount = null;
        String originalCurrency = null;

        if (dto.getCurrency() != null && !dto.getCurrency().equals("UZS")) {
            try {
                Map<String, Object> conversion = currencyService.convert(
                        dto.getCurrency(), "UZS", dto.getAmount().doubleValue());
                originalAmount = dto.getAmount().doubleValue();
                originalCurrency = dto.getCurrency();
                finalAmount = BigDecimal.valueOf(
                        ((Number) conversion.get("result")).doubleValue());
            } catch (Exception e) {
                // Kurs olishda xatolik — original summani saqlash
                finalAmount = dto.getAmount();
            }
        }

        Expense expense = Expense.builder()
                .title(dto.getTitle())
                .amount(finalAmount)
                .description(dto.getDescription())
                .date(dto.getDate())
                .receiptImageUrl(dto.getReceiptImageUrl())
                .currency("UZS")
                .originalAmount(originalAmount)
                .originalCurrency(originalCurrency)
                .category(category)
                .user(user)
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponse> getAll(String email) {
        User user = getUser(email);
        return expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponse> getByDateRange(
            String email, LocalDate start, LocalDate end) {
        User user = getUser(email);
        return expenseRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(user.getId(), start, end)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ExpenseResponse update(Long id, ExpenseDTO dto, String email) {
        User user = getUser(email);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Xarajat topilmadi!"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Ruxsat yo'q!");
        }

        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());

        return toResponse(expenseRepository.save(expense));
    }

    public void deleteAll(String email) {
        User user = getUser(email);
        List<Expense> expenses = expenseRepository
                .findByUserIdOrderByDateDesc(user.getId());
        expenseRepository.deleteAll(expenses);
    }

    public void delete(Long id, String email) {
        User user = getUser(email);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Xarajat topilmadi!"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Ruxsat yo'q!");
        }

        expenseRepository.delete(expense);
    }

    // Oylik hisobot + AI tahlil
    public Map<String, Object> getMonthlyReport(String email, int year, int month) {
        User user = getUser(email);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Object[]> categoryData = expenseRepository
                .sumByCategoryForUser(user.getId(), start, end);

        Double total = expenseRepository
                .sumByUserIdAndDateBetween(user.getId(), start, end);

        StringBuilder reportData = new StringBuilder();
        reportData.append(String.format("Oy: %d/%d\n", month, year));
        reportData.append(String.format("Jami: %.2f so'm\n", total != null ? total : 0));
        reportData.append("Kategoriyalar:\n");

        Map<String, Double> categoryMap = new LinkedHashMap<>();
        for (Object[] row : categoryData) {
            String catName = (String) row[0];
            Double amount = ((Number) row[1]).doubleValue();
            categoryMap.put(catName, amount);
            reportData.append(String.format("- %s: %.2f\n", catName, amount));
        }

        String aiAnalysis = aiService.analyzeMonthlyReport(reportData.toString());
        String savingAdvice = aiService.getSavingAdvice(reportData.toString());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", month);
        result.put("year", year);
        result.put("total", total != null ? total : 0);
        result.put("categories", categoryMap);
        result.put("aiAnalysis", aiAnalysis);
        result.put("savingAdvice", savingAdvice);

        return result;
    }

    // OCR: rasmdan xarajat yaratish
    public Map<String, String> extractFromReceipt(String base64Image) {
        String jsonResult = aiService.extractFromReceipt(base64Image);
        return Map.of("data", jsonResult);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi!"));
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .amount(e.getAmount())
                .description(e.getDescription())
                .date(e.getDate())
                .categoryName(e.getCategory() != null ? e.getCategory().getName() : null)
                .categoryIcon(e.getCategory() != null ? e.getCategory().getIcon() : null)
                .categoryColor(e.getCategory() != null ? e.getCategory().getColor() : null)
                .receiptImageUrl(e.getReceiptImageUrl())
                .currency(e.getCurrency() != null ? e.getCurrency() : "UZS")
                .originalAmount(e.getOriginalAmount())
                .originalCurrency(e.getOriginalCurrency())
                .createdAt(e.getCreatedAt())
                .build();
    }
}