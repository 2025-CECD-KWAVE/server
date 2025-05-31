package com.example.kwave.domain.news.dto;


import com.example.kwave.domain.news.domain.News;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import com.example.kwave.domain.news.domain.News;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {

    @JsonProperty("news_id")
    private String newsId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("published_at")
    private OffsetDateTime publishedAt;

    @JsonProperty("enveloped_at")
    private OffsetDateTime envelopedAt;

    @JsonProperty("dateline")
    private OffsetDateTime dateline;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("byline")
    private String byline;

    @JsonProperty("provider_link_page")
    private String providerLinkPage;

    @JsonProperty("category")
    private List<String> category;

    @JsonProperty("category_incident")
    private List<String> categoryIncident;

    @JsonProperty("hilight")
    private String hilight;

    public News toEntity() {
        return News.builder()
                .newsId(this.newsId)
                .title(this.title)
                .content(this.content)
                .publishedAt(this.publishedAt)          // ✅ toLocalDateTime() 제거
                .envelopedAt(this.envelopedAt)          // ✅ 제거
                .dateline(this.dateline)                // ✅ 제거
                .provider(this.provider)
                .byline(this.byline)
                .providerLinkPage(this.providerLinkPage)
                .category(this.category)
                .categoryIncident(this.categoryIncident)
                .build();
    }
}

