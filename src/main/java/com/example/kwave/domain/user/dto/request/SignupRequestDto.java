package com.example.kwave.domain.user.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SignupRequestDto {
    private String username;
    private String email;
    private String password;
    private String nationality;
    private String language;
    private List<String> preferredCategories;
}
