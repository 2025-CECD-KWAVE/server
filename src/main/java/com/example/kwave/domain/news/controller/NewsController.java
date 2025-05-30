package com.example.kwave.domain.news.controller;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.service.NewsService;
import com.example.kwave.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @PostMapping
    public ResponseEntity<String> saveNews(@RequestBody News news) {
        newsService.saveNewsIfNotExists(news);
        return ResponseEntity.ok("News saved (if not already exists).");
    }

    // 예: http://localhost:8080/api/news/fetch/01101001.20210107151933001
    @GetMapping("/fetch-all")
    public ResponseEntity<String> fetchAll(@RequestParam String from, @RequestParam String to) {
        try {
            newsService.fetchAndSaveAll(from, to);
            return ResponseEntity.ok("뉴스 저장 완료 (범위: " + from + " ~ " + to + ")");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("뉴스 저장 실패: " + e.getMessage());
        }
    }

    @PostMapping("/{newsId}/watch")
    public ResponseEntity<String> watchNews(@RequestParam UUID userId, @PathVariable String newsId) {
        newsService.userWatched(userId, newsId);
        return ResponseEntity.ok("뉴스 시청");
    }

    @GetMapping("/test-scheduler")
    public ResponseEntity<String> testScheduler() {
        try {
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
            String from = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String until = today.plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            newsService.fetchAndSaveAll(from, until);
            return ResponseEntity.ok("테스트: " + today + " 날짜 뉴스 수동 저장 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("테스트 실패: " + e.getMessage());
        }
    }
}