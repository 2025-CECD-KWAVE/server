package com.example.kwave.domain.recommend.controller;

import com.example.kwave.domain.recommend.dto.NewsReactionReqDto;
import com.example.kwave.domain.recommend.service.NewsReactionService;
import com.example.kwave.domain.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/react-news")
public class NewsReactionController {

    private final NewsReactionService service;

    @PostMapping
    public ResponseEntity<Void> reactNews(@AuthenticationPrincipal CustomUserDetails user,
                              @RequestBody NewsReactionReqDto requestDto) {

        requestDto.setUserId(user.getId());
        service.saveReaction(requestDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteNewsReaction(@AuthenticationPrincipal CustomUserDetails user,
                                   @RequestBody NewsReactionReqDto requestDto) {

        requestDto.setUserId(user.getId());
        service.deleteReaction(requestDto);
        return ResponseEntity.ok().build();
    }
}
