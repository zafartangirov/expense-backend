package com.expense.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Ism bo'sh bo'lmasin")
    private String fullName;

    @Email(message = "Email noto'g'ri")
    @NotBlank(message = "Email bo'sh bo'lmasin")
    private String email;

    @Size(min = 8, message = "Parol kamida 8 ta belgi bo'lsin")
    @NotBlank(message = "Parol bo'sh bo'lmasin")
    private String password;
}