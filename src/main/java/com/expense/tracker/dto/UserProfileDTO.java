package com.expense.tracker.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private int totalExpenses;
    private double totalAmount;
    private String profilePicture;
}