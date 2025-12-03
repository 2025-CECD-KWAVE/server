package com.example.kwave.domain.news.controller;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.dto.NewsDTO;
import com.example.kwave.domain.news.dto.NewsDetailDTO;
import com.example.kwave.domain.news.dto.NewsSummaryDTO;
import com.example.kwave.domain.news.service.NewsService;
import com.example.kwave.domain.translate.domain.TargetLangCode;
import com.example.kwave.domain.translate.domain.TranslatedNewsSummary;
import com.example.kwave.domain.translate.service.TranslatedNewsService;
import com.example.kwave.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "News", description = "뉴스 저장 API")
public class NewsController {

    private final NewsService newsService;
    private final TranslatedNewsService translatedNewsService;

    @PostMapping
    @Operation(summary = "개별 뉴스 저장 (테스트)", description = "개별 뉴스를 저장합니다")
    public ResponseEntity<String> saveNews(@RequestBody News news) {
        newsService.saveNewsIfNotExists(news);
        return ResponseEntity.ok("News saved (if not already exists).");
    }

    // 예: http://localhost:8080/api/news/fetch/01101001.20210107151933001
    @GetMapping("/fetch-all")
    @Operation(summary = "뉴스 API 호출 및 저장 (테스트)", description = "API를 통해 기간내의 뉴스를 저장합니다 (테스트)")
    public ResponseEntity<String> fetchAll(@RequestParam String from, @RequestParam String to) {
        try {
            newsService.fetchAndSaveAll(from, to);
            return ResponseEntity.ok("뉴스 저장 완료 (범위: " + from + " ~ " + to + ")");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("뉴스 저장 실패: " + e.getMessage());
        }
    }

    @GetMapping("/test-scheduler")
    @Operation(summary = "스케줄러 동작 (테스트)", description = "뉴스 저장 스케줄러를 동작시킵니다")
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


    @GetMapping("/list")
    @Operation(summary = "최신 뉴스 목록 조회", description = "최신순으로 뉴스 목록을 조회합니다")
    public ResponseEntity<List<NewsSummaryDTO>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Locale locale) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        List<NewsSummaryDTO> newsList = newsService.getNewsSummaries(pageable);

        TargetLangCode targetLangCode = translatedNewsService.convertLocaleToTargetLangCode(locale);

        if (targetLangCode == TargetLangCode.KO) {
            return ResponseEntity.ok(newsList);
        }
        else {
              return ResponseEntity.ok(translatedNewsService.getOrTranslateSummary(newsList, targetLangCode));
        }

    }

    @GetMapping("/{newsId}")
    @Operation(summary = "상세 뉴스 조회", description = "해당 newsId 뉴스 조회")
    public ResponseEntity<NewsDetailDTO> getNewsDetail(@PathVariable String newsId, Locale locale) {

        TargetLangCode targetLangCode = translatedNewsService.convertLocaleToTargetLangCode(locale);

        NewsDetailDTO newsDetailDTO = newsService.getNewsDetail(newsId);

        if (targetLangCode == TargetLangCode.KO) {
            return ResponseEntity.ok(newsDetailDTO);
        }
        else {
            return ResponseEntity.ok(translatedNewsService.getOrTranslateDetail(newsDetailDTO, targetLangCode));
        }
    }
}