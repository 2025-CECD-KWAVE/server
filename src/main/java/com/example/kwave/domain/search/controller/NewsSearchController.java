package com.example.kwave.domain.search.controller;

import com.example.kwave.domain.search.dto.request.NewsSearchReqDto;
import com.example.kwave.domain.search.dto.response.NewsSearchResDto;
import com.example.kwave.domain.search.service.NewsSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class NewsSearchController {
    private final NewsSearchService newsSearchService;

    /**
     * 뉴스 검색
     */
    @GetMapping
    public ResponseEntity<NewsSearchResDto> searchNews(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.info("뉴스 검색 요청: query={}, page={}, size={}", query, page, size);

        NewsSearchReqDto newsSearchReqDto = new NewsSearchReqDto(query, page, size);
        NewsSearchResDto response = newsSearchService.searchNews(newsSearchReqDto);

        log.info("뉴스 검색 완료: totalHits={}, resultCount={}",
                response.getTotalHits(),
                response.getResults().size());

        return ResponseEntity.ok(response);
    }
}
