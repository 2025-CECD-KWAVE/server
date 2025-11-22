package com.example.kwave.domain.recommend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class NewsRecommendReqDto {

    private int page; // 0부터 시작
    private int size; // 한 페이지에 몇 개 가져올 것인가?
}
