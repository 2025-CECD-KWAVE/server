package com.example.kwave.domain.news.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDetailDTO {
    private String newsId;
    private String title;
    private String content;
    private String provider;
    private String byline;
    private LocalDateTime publishedAt;
    private String providerLinkPage;
}