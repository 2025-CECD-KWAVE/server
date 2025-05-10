package com.example.kwave.domain.translate.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@AllArgsConstructor
@RedisHash("TranslatedNews")
public class TranslatedNewsTitle {

    @Id
    private String redisKey; // newsId:targetLangCode:Title 을 redis key로

    private String translatedTitle;
}
