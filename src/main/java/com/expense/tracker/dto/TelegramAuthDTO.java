package com.expense.tracker.dto;

import lombok.Data;

@Data
public class TelegramAuthDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;
    private Long authDate;
    private String hash;
}