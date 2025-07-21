package com.example.kwave.domain.user.controller;

import com.example.kwave.domain.translate.domain.TargetLangCode;
import com.example.kwave.domain.translate.service.TranslatedNewsService;
import com.example.kwave.domain.user.domain.CustomUserDetails;
import com.example.kwave.domain.user.dto.request.RecommendRequestDto;
import com.example.kwave.domain.user.dto.response.RecommendResponseDto;
import com.example.kwave.domain.user.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;
    private final TranslatedNewsService translatedNewsService;

    @PostMapping
    public ResponseEntity<RecommendResponseDto> recommend(@AuthenticationPrincipal CustomUserDetails user, Locale locale) {
        RecommendRequestDto requestDto = new RecommendRequestDto();
        requestDto.setUserId(user.getId());
        RecommendResponseDto response = recommendService.recommendNews(requestDto);
        TargetLangCode targetLangCode = translatedNewsService.convertLocaleToTargetLangCode(locale);

        if (targetLangCode == TargetLangCode.KO) {
            return ResponseEntity.ok(response);
        }
        else {
            RecommendResponseDto translatedResponse = RecommendResponseDto.builder()
                    .recommendedNews(translatedNewsService.getOrTranslateSummary(response.getRecommendedNews(), targetLangCode))
                    .build();
            return ResponseEntity.ok(translatedResponse);
        }
    }
}
