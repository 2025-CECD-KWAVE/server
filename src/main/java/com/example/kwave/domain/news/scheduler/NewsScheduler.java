package com.example.kwave.domain.news.scheduler;

import com.example.kwave.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsService newsService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Scheduled(fixedRate = 30 * 60 * 1000) // 30분 간격 실행
    public void fetchLatestNews() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String from = today.format(formatter);
        String until = today.plusDays(1).format(formatter);

        try {
            newsService.fetchAndSaveAll(from, until);
            log.info("[뉴스 스케줄러] {} 날짜 뉴스 저장 완료", today);
        } catch (Exception e) {
            log.error("[뉴스 스케줄러] 뉴스 저장 실패: {}", e.getMessage());
        }
    }
}
