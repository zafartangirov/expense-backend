package com.expense.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserDTO {

    @NotBlank(message = "Ism bo'sh bo'lmasin")
    private String fullName;

    @Email(message = "Email noto'g'ri")
    @NotBlank(message = "Email bo'sh bo'lmasin")
    private String email;
}