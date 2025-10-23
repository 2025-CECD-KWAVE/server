package com.example.kwave.domain.ai.scheduler;

import com.example.kwave.domain.ai.service.NewsProcessService;
import com.example.kwave.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsProcessScheduler {

    private final NewsProcessService newsProcessService;

    /**
     * 뉴스 수집 후 요약 및 벡터화 배치 실행
     * 요약 및 벡터화: 새벽 2시 실행
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleNewsProcessing() {
        log.info("=== 뉴스 요약/벡터화 배치 시작 ===");

        try {
            newsProcessService.processUnprocessedNews();
            log.info("=== 뉴스 요약/벡터화 배치 성공 ===");
        }
        catch (Exception e) {
            log.error("=== 뉴스 요약/벡터화 배치 실패 ===");
        }

    }
}
