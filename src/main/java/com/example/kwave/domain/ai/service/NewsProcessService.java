package com.example.kwave.domain.ai.service;

import com.example.kwave.domain.ai.domain.NewsVectorDoc;
import com.example.kwave.domain.ai.domain.repository.NewsVectorRepository;
import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsProcessService {

    private final OpenAiService openAiService;
    private final NewsRepository newsRepository;
    private final NewsVectorRepository newsVecRepository;

    /**
     * 오늘 수집된 뉴스 중 summary가 null인 것만 처리
     */
    @Transactional
    public void processUnprocessedNews() {
        // 오늘 날짜 범위 설정
        LocalDate today =  LocalDate.now();
        OffsetDateTime startAt = today.atStartOfDay().atOffset(ZoneOffset.of("+09:00"));
        OffsetDateTime endAt = today.atTime(23, 59, 59).atOffset(ZoneOffset.of("+09:00"));

        // 오늘 발행되고 summary가 null인 뉴스 목록 조회
        List<News> unprocessedNews = newsRepository
                .findBySummaryIsNullAndPublishedAtBetween(startAt, endAt);

        if (unprocessedNews.isEmpty()) {
            log.info("처리할 뉴스가 없습니다.");
            return;
        }

        log.info("처리할 뉴스 개수: {} (오늘 수집된 뉴스)",  unprocessedNews.size());

        int success = 0, fail = 0;

        for (News news : unprocessedNews) {
            try {
                processNews(news);
                success++;
                log.info("처리 성공 [{} / {}]: {}", success, unprocessedNews.size(), news.getNewsId());
            }
            catch (Exception e) {
                fail++;
                log.error("처리 실패 - newsId: {}, error: {}", news.getNewsId(), e.getMessage());
            }
        }

        log.info("배치 처리 완료 - 성공: {}, 실패: {}", success, fail);
    }

    /**
     * 단일 뉴스 처리
     */
    @Transactional
    public void processNews(News news) {

        // 요약 생성
        String summary = openAiService.summarize(news.getContent());
        log.info("요약 완료 - newsId: {}", news.getNewsId());

        news.setSummary(summary);
        newsRepository.save(news);

        // 임베딩 벡터 생성
        float[] embedding = openAiService.embed(summary);
        log.info("임베딩 완료 - newsId: {}, 차원: {}", news.getNewsId(), embedding.length);

        // OpenSearch에 저장
        NewsVectorDoc doc = NewsVectorDoc.builder()
                .id(news.getNewsId())
                .newsId(news.getNewsId())
                .newsContent(news.getContent())
                .newsSummary(summary)
                .embedding(embedding)
                .build();

        newsVecRepository.save(doc);
        log.info("OpenSearch 저장 완료 - newsId: {}", news.getNewsId());
    }


    /**
     * 특정 뉴스 처리
     */
    @Transactional
    public void processNewsByNewsId(String newsId) {
        News news = newsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다: " + newsId));

        processNews(news);
    }

    /**
     * Opensearch에서 뉴스 벡터 조회
     */
    public NewsVectorDoc getNewsVector(String newsId) {
        return newsVecRepository.findByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("벡터 데이터를 찾을 수 없습니다: " + newsId));
    }
}
