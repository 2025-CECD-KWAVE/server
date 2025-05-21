package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.user.dto.request.RecommendRequestDto;
import com.example.kwave.domain.user.dto.response.RecommendResponseDto;
import com.example.kwave.domain.user.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @PostMapping
    public ResponseEntity<RecommendResponseDto> recommend(@RequestBody RecommendRequestDto requestDto) {
        RecommendResponseDto response = recommendService.recommendNews(requestDto);
        return ResponseEntity.ok(response);
    }
}
