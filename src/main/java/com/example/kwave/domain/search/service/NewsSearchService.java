package com.example.kwave.domain.search.service;

import com.example.kwave.domain.ai.domain.NewsVectorDoc;
import com.example.kwave.domain.search.dto.request.NewsSearchReqDto;
import com.example.kwave.domain.search.dto.response.NewsSearchResDto;
import com.example.kwave.domain.search.dto.response.SearchingResultDto;
import com.example.kwave.domain.search.repository.NewsSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsSearchService {

    private final NewsSearchRepository newsSearchRepository;

    /**
     * KNN 벡터 기반 뉴스 검색
     */
    public NewsSearchResDto searchNews(NewsSearchReqDto newsSearchReqDto) {
        log.info("뉴스 검색: query={}", newsSearchReqDto.getQuery());

        validateSearchRequest(newsSearchReqDto);

        NewsSearchRepository.SearchResult searchResult = newsSearchRepository.searchNews(
                newsSearchReqDto.getQuery(),
                newsSearchReqDto.getPage(),
                newsSearchReqDto.getPageSize()
        );

        List<SearchingResultDto> results = convertToResultDtos(searchResult);

        log.info("검색 완료: {} 문서", results.size());

        return NewsSearchResDto.builder()
                .results(results)
                .totalHits(searchResult.getTotalHits())
                .currentPage(newsSearchReqDto.getPage())
                .pageSize(newsSearchReqDto.getPageSize())
                .build();
    }

    /**
     * 뉴스 검색 쿼리 검증
     * @param newsSearchReqDto
     */
    private void validateSearchRequest(NewsSearchReqDto newsSearchReqDto) {
        if (newsSearchReqDto.getQuery() == null || newsSearchReqDto.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        if (newsSearchReqDto.getPageSize() <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }

        if (newsSearchReqDto.getPageSize() > 50) {
            throw new IllegalArgumentException("한 번에 최대 50개까지 조회 가능합니다.");
        }

        if (newsSearchReqDto.getPage() < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
    }

    private List<SearchingResultDto> convertToResultDtos(NewsSearchRepository.SearchResult searchResult) {
        List<NewsVectorDoc> documents = searchResult.getDocuments();
        List<Float> scores = searchResult.getScores();

        if (documents.isEmpty()) {
            return List.of();
        }

        List<SearchingResultDto> results = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            NewsVectorDoc doc = documents.get(i);
            float score = scores.get(i);

            // ⭐ 절대적 점수 기준으로 계산
            int relevance = calculateAbsoluteRelevance(score);

            results.add(SearchingResultDto.builder()
                    .newsId(doc.getNewsId())
                    .content(doc.getNewsContent())
                    .summary(doc.getNewsSummary())
                    .relevance(relevance)
                    .build());
        }

        return results;
    }

    /**
     * 절대적 유사도 계산 (코사인 유사도 기반)
     *
     * OpenSearch KNN의 score는 코사인 유사도 기반:
     * - 1.0: 완전히 동일한 벡터 (100% 일치)
     * - 0.9: 매우 유사 (~90%)
     * - 0.7: 관련성 높음 (~70%)
     * - 0.5: 중간 정도 (~50%)
     * - 0.3 이하: 관련성 낮음
     *
     * @param score OpenSearch KNN 점수 (보통 0.0~1.0 범위)
     * @return 0~100 사이의 relevance 점수
     */
    private int calculateAbsoluteRelevance(float score) {
        if (Float.isNaN(score) || score <= 0.0f) {
            return 0;
        }

        // 점수를 그대로 백분율로 변환
        // score = 0.65 → relevance = 65
        // score = 0.95 → relevance = 95
        // score = 1.0  → relevance = 100
        float percentage = score * 100;

        // 100 초과 방지
        return Math.min(100, Math.round(percentage));
    }
}