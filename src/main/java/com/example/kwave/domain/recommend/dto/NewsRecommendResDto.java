package com.example.kwave.domain.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class NewsRecommendResDto {

    private List<String> newsIds; // 추천된 뉴스 id 리스트
    private int page;
    private int size;
}
