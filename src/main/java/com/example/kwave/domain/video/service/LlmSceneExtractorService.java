package com.example.kwave.domain.video.service;

import com.example.kwave.domain.news.domain.News;
import com.example.kwave.domain.news.domain.repository.NewsRepository;
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

        News news = newsRepository.findByNewsId(newsId)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        String newsText = news.getSummary() != null ? news.getSummary() : news.getContent();
        List<String> imageUrls = news.getImageUrls();

        String systemPrompt = """
                You are an AI Video Director Assistant.
                
                Your task is to analyze the provided news text and extract exactly 5 scenes that are most suitable for short-form visual representation.
                
                Each scene prompt must be designed as:
                - a 5-second clip,
                - framed specifically for a 9:16 vertical (shorts) video format.
                
                Output the result strictly as a JSON array. Each object in the array must follow this format:
                
                [
                  {
                    "sceneIndex": 1,
                    "description": "Describe the specific scene here in Korean.",
                    "extraPrompt": "Describe the visual prompts here in English (must explicitly mention 9:16 vertical framing and short 5-second composition)."
                  },
                  ...
                ]
                
                Constraints:
                1. The value of `description` must be written in Korean.
                2. The value of `extraPrompt` must be written in English and must clearly instruct a 5-second, 9:16 vertical video composition.
                
                3. EXTREME RESTRICTIONS ON PEOPLE:
                   - Do NOT describe any specific individual.
                   - Do NOT describe identifiable people (e.g., singer, performer, dancer, politician, reporter).
                   - Do NOT mention any human actions, poses, gestures, or facial expressions.
                   - Only anonymous, distant groups such as '군중', '사람들', '관객' may appear, and ONLY as background silhouettes.
                   - No recognizable humans, no single-person focus.
                
                4. STRICT BAN ON PROPER NOUNS:
                   - No real names, celebrity names, organization names, or location names.
                   - Use only generic scene descriptions (e.g., "도시 거리", "무대", "가상 공간").
                
                5. SCENE FOCUS RULE:
                   - Scenes must focus on environments, landscapes, objects, atmospheres, or symbolic visuals.
                   - Not people.
                
                6. FORMAT REQUIREMENTS:
                   - Each scene must be visually strong, simple, and optimized for a short 5-second 9:16 vertical video shot.
                   - No markdown, no explanations. Output ONLY the JSON array.
                
                Generate exactly 5 scenes.
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
