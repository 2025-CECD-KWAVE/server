package com.example.kwave.domain.recommend.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.recommend.domain.NewsViewHistory;
import com.example.kwave.domain.recommend.domain.repository.NewsViewHistoryRepository;
import com.example.kwave.domain.recommend.dto.NewsViewReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Service
public class NewsViewService {

    private final NewsViewHistoryRepository repository;
    private final NewsRepository newsRepository;

    /**
     * 뉴스 시청 이력 추가
     * @param dto
     */
    @Transactional
    public void recordView(NewsViewReqDto dto) {
        NewsViewHistory newsViewHistory = NewsViewHistory.builder()
                .userId(dto.getUserId())
                .newsId(dto.getNewsId())
                .viewedAt(OffsetDateTime.now())
                .build();

        // 뉴스 시청 이력 추가
        repository.save(newsViewHistory);

        // 뉴스 조회수 +1
        newsRepository.incrementViewCount(dto.getNewsId());
    }

    /**
     * 특정 뉴스 조회수 조회
     * @param newsId
     * @return
     */
    @Transactional(readOnly = true)
    public Long getViewCount(String newsId) {
        return newsRepository.findByNewsId(newsId)
                .map(News::getViewCount)
                .orElse(0L);
    }
}
