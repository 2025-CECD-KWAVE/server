package com.example.kwave.domain.recommend.service;

import com.example.kwave.domain.ai.domain.NewsVectorDoc;
import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import com.example.kwave.domain.recommend.domain.NewsReaction;
import com.example.kwave.domain.recommend.domain.ReactionType;
import com.example.kwave.domain.recommend.domain.repository.NewsReactionRepository;
import com.example.kwave.domain.recommend.dto.NewsRecommendResDto;
import com.example.kwave.domain.search.repository.NewsSearchRepository;
import com.example.kwave.domain.user.domain.User;
import com.example.kwave.domain.user.domain.repository.UserRepository;
import com.example.kwave.domain.user.service.UserPreferenceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsRecommendService {

    private final UserPreferenceCacheService userPrefCache;
    private final NewsReactionRepository newsReactionRepository;
    private final UserRepository userRepository;
    private final NewsSearchRepository newsSearchRepository;
    private final NewsRepository newsRepository;

    public NewsRecommendResDto recommendNews(UUID userId, int page, int size) {
        log.info("뉴스 추천 요청 - userId = {}, page = {}, size = {}", userId, page, size);

        // 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found: " + userId));

        // 선호 벡터 로딩
        float[] userVec = userPrefCache.loadUserVector(userId);

        // Dislike한 뉴스 ID 목록 (DB조회)
        List<String> dislikedNewsIds = newsReactionRepository
                .findByUserIdAndReactionType(userId, ReactionType.Dislike)
                .stream()
                .map(NewsReaction::getNewsId)
                .collect(Collectors.toList());

        List<NewsRecommendResDto.IdWithScore> resultList;

        if (userVec == null) {
            log.info("Cold Starting - userId = {}", userId);
            resultList = getColdStartNewsIds(dislikedNewsIds, page, size);
        }
        else {
            resultList = getPersonalizedNewsIds(userVec, dislikedNewsIds, page, size);
        }

        return NewsRecommendResDto.builder()
                .newsIds(resultList)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Cold Start: 선호 벡터가 없는 유저에게 최신 뉴스 추천
     * Dislike한 뉴스만 제외
     */
    private List<NewsRecommendResDto.IdWithScore> getColdStartNewsIds(List<String> dislikeNewsIds, int page, int size) {
        Page<News> newsPage;

        // 싫어요 뉴스 제외 후 뉴스 최신 순으로 반환 -> cold start라 없을 수 있음
        if (dislikeNewsIds == null || dislikeNewsIds.isEmpty()) {
            newsPage = newsRepository.findAllByOrderByEnvelopedAtDesc(PageRequest.of(page, size));
        }
        else {
            newsPage = newsRepository
                    .findByNewsIdNotInOrderByEnvelopedAtDesc(dislikeNewsIds, PageRequest.of(page, size));
        }

        // Cold Start이므로 점수 X
        return newsPage.stream()
                .map(news -> NewsRecommendResDto.IdWithScore.builder()
                        .newsId(news.getNewsId())
                        .score(0.0f)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Personalized: 선호 벡터 + Dislike 필터를 이용한 OpenSearch KNN 추천
     */
    private List<NewsRecommendResDto.IdWithScore> getPersonalizedNewsIds(
                    float[] userVec, List<String> dislikedNewsIds, int page, int size) {

        NewsSearchRepository.KnnResult result =
                newsSearchRepository.searchByVector(userVec, dislikedNewsIds, page, size);

        List<NewsVectorDoc> docs = result.getDocuments();
        List<Float> scores = result.getScores();

        List<NewsRecommendResDto.IdWithScore> list = new ArrayList<>();

        for (int i = 0; i < docs.size(); i++) {
            float score = (scores.size() > i) ? scores.get(i) : 0.0f;

            list.add(NewsRecommendResDto.IdWithScore.builder()
                    .newsId(docs.get(i).getNewsId())
                    .score(score)
                    .build());
        }
        return list;
    }
}
