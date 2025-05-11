package com.example.kwave.domain.user.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RecommendResponseDto {
    //추천 결과 뉴스 ID 반환
    private List<String> recommendedNewsIds;
}
