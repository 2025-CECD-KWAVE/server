package com.example.kwave.domain.news.dto;


import com.example.kwave.domain.news.domain.News;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {

    private String newsId;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private LocalDateTime envelopedAt;
    private LocalDateTime dateline;
    private String provider;
    private String byline;
    private String providerLinkPage;
    private List<String> category;
    private List<String> categoryIncident;

    public News toEntity() {
        return News.builder()
                .newsId(this.newsId)
                .title(this.title)
                .content(this.content)
                .publishedAt(this.publishedAt)
                .envelopedAt(this.envelopedAt)
                .dateline(this.dateline)
                .provider(this.provider)
                .byline(this.byline)
                .providerLinkPage(this.providerLinkPage)
                .category(this.category)
                .categoryIncident(this.categoryIncident)
                .build();
    }
}