package com.example.kwave.domain.translate.domain;

import com.example.kwave.domain.news.dto.NewsSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@AllArgsConstructor
@RedisHash("TranslatedNews")
public class TranslatedNewsSummary {

    @Id
    private String redisKey; // newsId:targetLangCode:Summary 을 redis key로
    private String translatedTitle;
    private String translatedSummary;
    private String timeAgo; // 예: "5분 전", "3시간 전"

    public NewsSummaryDTO toNewsSummaryDto() {
        return NewsSummaryDTO.builder()
                .newsId(redisKey.split(":")[0])
                .title(translatedTitle)
                .summary(translatedSummary)
                .timeAgo(timeAgo)
                .build();
    }
}
