package com.example.kwave.domain.translate.domain;

import com.example.kwave.domain.news.dto.NewsDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@RedisHash("TranslatedNews")
public class TranslatedNewsDetail {

    @Id
    private String redisKey; // newsId:targetLangCode:Detail 을 redis key로
    private String translatedTitle;
    private String translatedContent;
    private String translatedProvider;
    private String translatedByline;
    private LocalDateTime publishedAt;
    private String providerLinkPage;

    public NewsDetailDTO toNewsDetailDto(List<String> imageUrls) {
        return NewsDetailDTO.builder()
                .newsId(redisKey.split(":")[0])
                .title(translatedTitle)
                .content(translatedContent)
                .provider(translatedProvider)
                .byline(translatedByline)
                .publishedAt(publishedAt)
                .imageUrls(imageUrls)
                .providerLinkPage(providerLinkPage)
                .build();
    }
}

