package com.example.kwave.domain.news.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSummaryDTO {
    private String newsId;
    private String title;
    private String summary;
    private String timeAgo; // 예: "5분 전", "3시간 전"
    private String thumbnailUrl;

}