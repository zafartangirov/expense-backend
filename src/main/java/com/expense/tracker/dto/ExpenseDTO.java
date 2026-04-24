package com.expense.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDTO {

    @NotBlank(message = "Sarlavha bo'sh bo'lmasin")
    private String title;

    @NotNull(message = "Summa bo'sh bo'lmasin")
    @Positive(message = "Summa musbat bo'lsin")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Sana bo'sh bo'lmasin")
    private LocalDate date;

    private Long categoryId;
    private String receiptImageUrl;

    // Builder.Default o'rniga oddiy default
    private String currency = "UZS";
    private Double originalAmount;
    private String originalCurrency;
}