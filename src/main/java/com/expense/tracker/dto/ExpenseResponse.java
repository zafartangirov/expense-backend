package com.expense.tracker.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String receiptImageUrl;
    private LocalDateTime createdAt;

    private String currency;
    private Double originalAmount;
    private String originalCurrency;
}