package com.expense.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotBlank(message = "Eski parol bo'sh bo'lmasin")
    private String oldPassword;

    @Size(min = 6, message = "Yangi parol kamida 6 ta belgi")
    @NotBlank(message = "Yangi parol bo'sh bo'lmasin")
    private String newPassword;
}