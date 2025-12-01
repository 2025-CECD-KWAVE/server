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