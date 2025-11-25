package com.example.kwave.domain.video.service;

import com.example.kwave.global.config.OpenAiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmSceneExtractorService {

    private final OpenAiConfig openAiConfig;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * 뉴스 본문을 입력받아 장면(Scene) JSON 리스트를 추출
     * - SceneDto 구조와 일치하는 JSON 배열을 반환
     * - 각 장면은 { "sceneIndex": int, "description": string, "extraPrompt": string }
     */
    public String extractScenes(String newsText) {
        RestTemplate restTemplate = new RestTemplate();
        /*
        // ✅ 시스템 프롬프트 (LLM의 역할 정의)
        String systemPrompt = """
            너는 영상 연출 보조 AI야.
            아래 뉴스 본문을 보고 시각적으로 표현하기 좋은 장면 4~5개를 뽑아.
            각 장면은 JSON 배열로 표현하고, 각 원소는 다음 형식을 따라:
            
            [
              {
                "sceneIndex": 1,
                "description": "무대 위에서 가수를 비추는 장면",
                "extraPrompt": "A dynamic concert stage with bright lights, cinematic style"
              },
              ...
            ]
            
            반드시 JSON 배열만 출력하고, 설명 문장은 절대 포함하지 마.
        """;
        */

        String systemPrompt = """
            You are an AI Video Director Assistant.

            Your task is to analyze the provided news text and extract 4 to 5 scenes that are most suitable for visual representation.

            Output the result strictly as a JSON array. Each object in the array must follow this format:

            [
            {
                "sceneIndex": 1,
                "description": "Describe the specific scene here in Korean.",
                "extraPrompt": "Describe the visual prompts here in English (e.g., lighting, style, objects)."
            },
            {
                "sceneIndex": 2,
                "description": "무대 위에서 가수를 비추는 장면",
                "extraPrompt": "A dynamic concert stage with bright lights, cinematic style"
              },
            ...
            ]

            **Constraints:**
            1. The value of `description` must be written in **Korean**.
            2. The value of `extraPrompt` must be written in **English**.
            3. **STRICTLY FORBIDDEN:** Do NOT use specific proper nouns, real names of people, or specific location names.
            - Instead, use generic terms describing their role or category.
            - Example: 'the singer' or 'the artist' (가수, 아티스트).
            - Example: 'the concert hall' or 'the venue' (공연장, 무대).
            4. Output **ONLY** the raw JSON array. Do not include markdown code blocks (```json) or any explanatory text.
        """;

        // ✅ 요청 본문 구성
        Map<String, Object> body = Map.of(
                "model", openAiConfig.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", newsText)
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
            if (result == null || result.get("choices") == null) {
                throw new RuntimeException("LLM 응답이 비정상입니다.");
            }

            // ✅ LLM 결과에서 텍스트 추출
            String content = ((Map<String, String>)
                    ((Map<String, Object>) ((List<?>) result.get("choices")).get(0))
                            .get("message")).get("content");

            if (content == null || content.isBlank()) {
                throw new RuntimeException("LLM이 빈 응답을 반환했습니다.");
            }

            log.info("🎬 LLM Scene JSON 결과:\n{}", content.trim());
            return content.trim(); // ✅ JSON 문자열 그대로 반환

        } catch (Exception e) {
            log.error("❌ LLM Scene 추출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("LLM Scene 추출 중 오류 발생", e);
        }
    }
}