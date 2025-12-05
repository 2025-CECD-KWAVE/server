package com.example.kwave.domain.recommend.controller;

import com.example.kwave.domain.recommend.dto.NewsRecommendReqDto;
import com.example.kwave.domain.recommend.dto.NewsRecommendResDto;
import com.example.kwave.domain.recommend.service.NewsRecommendService;
import com.example.kwave.domain.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news-recommend")
public class NewsRecommendController {

    private final NewsRecommendService newsRecommendService;

    /**
     * 뉴스 추천 (무한 스크롤 / 리스트 공용)
     * 프론트: page, size 조합으로 호출함
     */
    @PostMapping("/news")
    public ResponseEntity<NewsRecommendResDto> recommend(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody NewsRecommendReqDto reqDto
    ) {
        NewsRecommendResDto resDto =
                newsRecommendService.recommendNews(user.getId(), reqDto.getPage(), reqDto.getSize());
        return ResponseEntity.ok(resDto);
    }
}
