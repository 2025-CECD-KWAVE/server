package com.example.kwave.domain.user.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class RecommendRequestDto {
    private UUID userID; //사용자 식별
}
