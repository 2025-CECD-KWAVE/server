package com.example.kwave.domain.video.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.repository.NewsRepository;
import com.example.kwave.global.config.OpenAiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmSceneExtractorService {

    private final OpenAiConfig openAiConfig;
    private final NewsRepository newsRepository;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public String extractScenesWithImages(String newsId) {
        RestTemplate restTemplate = new RestTemplate();

        // ✅ 뉴스 조회
        News news = newsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        String newsText = news.getSummary() != null ? news.getSummary() : news.getContent();
        List<String> imageUrls = news.getImageUrls();

        // ✅ 시스템 프롬프트 정의
        String systemPrompt = """
            너는 영상 연출 보조 AI야.
            아래는 뉴스 본문과 기사에 포함된 이미지 목록이야.
            기사 내용을 시각적으로 표현하기 좋은 장면 4~5개를 만들어.
            
            각 장면은 JSON 배열로 반환하고, 형식은 아래와 같아:
            [
              {
                "sceneIndex": 1,
                "description": "무대 위에서 가수를 비추는 장면",
                "extraPrompt": "A dynamic concert stage with bright lights, cinematic style",
                "matchedImageUrl": "https://cdn.news.com/image_1.jpg"
              },
              ...
            ]

            조건:
            1. 이미지가 특정 장면에 어울린다면 matchedImageUrl에 URL을 넣어.
            2. 어울리는 이미지가 없다면 반드시 null을 넣어.
            3. JSON 배열만 출력하고, 부가설명은 포함하지 마.
        """;

        Map<String, Object> body = Map.of(
                "model", openAiConfig.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content",
                                "뉴스 본문:\n" + newsText + "\n\n이미지 목록:\n" + imageUrls)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result == null || result.get("choices") == null)
                throw new RuntimeException("LLM 응답이 비정상입니다.");

            String content = ((Map<String, String>)
                    ((Map<String, Object>) ((List<?>) result.get("choices")).get(0))
                            .get("message")).get("content");

            if (content == null || content.isBlank())
                throw new RuntimeException("LLM이 빈 응답을 반환했습니다.");

            log.info("🎬 LLM Scene + Image JSON 결과:\n{}", content.trim());
            return content.trim();

        } catch (Exception e) {
            log.error("❌ LLM Scene 추출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("LLM Scene 추출 중 오류 발생", e);
        }
    }
}
