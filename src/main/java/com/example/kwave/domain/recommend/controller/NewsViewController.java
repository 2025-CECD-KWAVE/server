package com.example.kwave.domain.recommend.controller;

import com.example.kwave.domain.recommend.domain.NewsViewHistory;
import com.example.kwave.domain.recommend.dto.NewsViewReqDto;
import com.example.kwave.domain.recommend.service.NewsViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/view-news")
public class NewsViewController {

    private final NewsViewService service;

    /**
     * 시청한 뉴스 기록 + 조회수 증가
     */
    @PostMapping("/view")
    public ResponseEntity<String> recordView(@RequestBody NewsViewReqDto dto) {
        service.recordView(dto);
        return ResponseEntity.ok("뉴스 시청 이력 수집 완료");
    }

    /**
     * 사용자가 본 뉴스 시청 이력 조회
     */
    @GetMapping("/view-history/{userId}")
    public ResponseEntity<List<NewsViewHistory>> getViewHistory(@PathVariable UUID userId) {
        List<NewsViewHistory> history = service.getViewHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * 특정 뉴스의 조회수 조회
     */
    @GetMapping("/view-count/{newsId}")
    public ResponseEntity<Long> getViewCount(@PathVariable String newsId) {
        Long viewCount = service.getViewCount(newsId);
        return ResponseEntity.ok(viewCount);
    }
}
