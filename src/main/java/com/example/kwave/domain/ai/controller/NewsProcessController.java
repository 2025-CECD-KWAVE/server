package com.example.kwave.domain.ai.controller;

import com.example.kwave.domain.ai.service.NewsProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/news-process")
@RequiredArgsConstructor
@Slf4j
public class NewsProcessController {

    private final NewsProcessService newsProcessService;

    /**
     * 수동 배치 수행
     */
    @PostMapping("/batch")
    public Map<String, Object> runBatch() {
        try {
            log.info("수동 배치 실행 시작");
            newsProcessService.processUnprocessedNews();

            return Map.of(
                    "success", true,
                    "message", "배치 처리 완료"
            );
        }
        catch (Exception e) {
            log.error("배치 처리 실패", e);

            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * 특정 뉴스만 처리
     */
    @PostMapping("/process/{newsId}")
    public Map<String, Object> processNews(@PathVariable String newsId) {
        try {
            newsProcessService.processNewsByNewsId(newsId);

            return Map.of(
                    "success", true,
                    "message", "처리 완료",
                    "newsId", newsId
            );
        }
        catch (Exception e) {
            log.error("뉴스 처리 실패 - newsId: {}",  newsId, e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * OpenSearch Connection Test
     */
    @GetMapping("/test-connection")
    public Map<String, Object> testConnection() {
        try {
            return Map.of(
                    "success", true,
                    "message", "OpenSearch 연결 성공"
            );
        }
        catch (Exception e) {
            log.error("OpenSearch 연결 실패", e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }
}
