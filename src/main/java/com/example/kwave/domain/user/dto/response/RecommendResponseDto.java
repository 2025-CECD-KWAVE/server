package com.example.kwave.domain.user.dto.response;

import com.example.kwave.domain.news.dto.NewsSummaryDTO;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecommendResponseDto {
    //추천 결과 News Summary 반환
    private List<NewsSummaryDTO> recommendedNews;
}
