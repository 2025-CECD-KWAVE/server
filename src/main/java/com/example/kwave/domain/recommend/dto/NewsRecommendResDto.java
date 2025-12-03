package com.example.kwave.domain.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class NewsRecommendResDto {

    private List<IdWithScore> newsIds; // 추천된 뉴스 id 리스트
    private int page;
    private int size;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class IdWithScore {
        private String newsId;
        private float score;
    }
}
