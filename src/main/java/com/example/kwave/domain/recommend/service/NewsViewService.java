package com.example.kwave.domain.recommend.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.recommend.domain.NewsViewHistory;
import com.example.kwave.domain.recommend.domain.repository.NewsViewHistoryRepository;
import com.example.kwave.domain.recommend.dto.NewsViewReqDto;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NewsViewService {

    private final UserRepository userRepository;
    private final NewsViewHistoryRepository repository;
    private final NewsRepository newsRepository;

    /**
     * 뉴스 시청 이력 추가
     * @param dto
     */
    @Transactional
    public void recordView(NewsViewReqDto dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new RuntimeException("User not found: " + dto.getUserId());
        }

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
     * 사용자의 최근 뉴스 시청 이력 조회
     */
    @Transactional(readOnly = true)
    public List<NewsViewHistory> getViewHistory(UUID userId) {
        Pageable page = PageRequest.of(0, 20);
        return repository.findByUserIdOrderByViewedAtDesc(userId, page);
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
