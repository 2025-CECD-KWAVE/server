package com.example.kwave.domain.news.service;

import com.example.kwave.domain.news.domain.NewsEmbedding;
import com.example.kwave.domain.news.domain.repository.NewsEmbeddingRepository;
import com.example.kwave.domain.news.dto.NewsEmbeddingDto;
import com.example.kwave.global.util.FloatArrayConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsEmbeddingService {

    private final NewsEmbeddingRepository repository;

    /**
     * news_id에 대한 임베딩 저장 또는 업데이트
     */
    public void saveEmbedding(String newsId, float[] embedding) {
        byte[] bytes = FloatArrayConverter.toBytes(embedding);

        NewsEmbedding newsEmbedding = NewsEmbedding.builder()
                .newsId(newsId)
                .embedding(bytes)
                .build();

        repository.save(newsEmbedding);
    }

    /**
     * 각 사용자의 선호 벡터 연산을 위한 뉴스 임베딩벡터 조회
     */
    public Optional<NewsEmbeddingDto> getEmbedding(String newsId) {
        return repository.findById(newsId)
                .map(entity -> NewsEmbeddingDto.builder()
                        .newsId(entity.getNewsId())
                        .embedding(FloatArrayConverter.toFloatArray(entity.getEmbedding()))
                        .build()
                );
    }

    /**
     * 임베딩 존재 여부 확인
     */
    public boolean exists(String newsId) {
        return repository.existsById(newsId);
    }
}
