package com.example.kwave.domain.ai.controller;

import com.example.kwave.domain.ai.service.OpenAiService;
import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class OpenAiController {

    private final NewsRepository newsRepository;
    private final OpenAiService openAiService;

    /**
     * 테스트: 특정 뉴스 ID로 요약 + 벡터화
     */
    @PostMapping("/process")
    public Map<String, Object> testProcess(@RequestParam String newsId) {
        try {
            // DB에서 뉴스 가져오기
            News news = newsRepository.findByNewsId(newsId)
                    .orElseThrow(() -> new RuntimeException("뉴스 없음: " + newsId));

            log.info("뉴스 찾음: {}", news.getTitle());

            // 원문 요약
            String summary = openAiService.summarize(news.getContent());
            log.info("요약 완료: {}", summary);

            // 요약문 벡터화
            float[] embedding = openAiService.embed(summary);
            log.info("벡터화 완료. 차원: {}", embedding.length);

            // DB에 요약 저장
            news.setSummary(summary);
            newsRepository.save(news);

            // 결과 리턴
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("newsId", newsId);
            result.put("title", news.getTitle());
            result.put("summary", summary);
            result.put("embeddingDimension", embedding.length);
            result.put("embeddingSample", new float[]{
                    embedding[0], embedding[1], embedding[2]
            });

            return result;

        } catch (Exception e) {
            log.error("처리 실패", e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }
}
