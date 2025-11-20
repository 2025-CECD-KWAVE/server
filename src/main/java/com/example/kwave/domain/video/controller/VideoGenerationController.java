package com.example.kwave.domain.video.controller;

import com.example.kwave.domain.video.service.NewsVideoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoGenerationController {

    private final NewsVideoService newsVideoService;

    public VideoGenerationController(NewsVideoService newsVideoService) {
        this.newsVideoService = newsVideoService;
    }

    /**
     * 테스트용 - 특정 뉴스 ID로 title + content를 합쳐 반환
     */
    @GetMapping("/scenes/{newsId}")
    public String generateScenes(@PathVariable String newsId) {
        return newsVideoService.getScenesFromNews(newsId);
    }

    @GetMapping("/generate/{newsId}")
    public List<String> generateVideo(@PathVariable String newsId) {
        return newsVideoService.generateVideoFromNews(newsId);
    }
}