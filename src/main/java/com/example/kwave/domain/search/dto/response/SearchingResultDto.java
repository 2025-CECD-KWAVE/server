package com.example.kwave.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchingResultDto {

    private String newsId;

    private String content;

    private String summary;

    // 유사도 점수
    private Integer relevance;
}
