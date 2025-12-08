package com.example.kwave.domain.ai.service;

import com.example.kwave.domain.ai.dto.request.ChatRequest;
import com.example.kwave.domain.ai.dto.request.EmbeddingRequest;
import com.example.kwave.domain.ai.dto.response.ChatResponse;
import com.example.kwave.domain.ai.dto.response.EmbeddingResponse;
import com.example.kwave.global.config.OpenAiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final OpenAiConfig config;

    private static final Pattern KOREAN_PATTERN = Pattern.compile(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

    /**
     * 다국어 검색어 -> 한국어 번역 (K-Pop 엔티티 최적화)
     */
    public String translateSearchQuery(String query) {
        if (isKorean(query)) {
            return query;
        }

        log.info("검색어 번역 수행 (Foreign -> KR): '{}'", query);

        // 프롬프트 엔지니어링 강화
        // 역할 부여: K-Pop 뉴스 검색 번역가
        // 제약 사항: 직역 금지
        // 예시 제공: G-Dragon -> 지드래곤 등 명확한 예시 제공
        String systemPrompt = """
            You are a specialized translator for a K-Pop news search engine.
            Your task is to translate the user's search query into natural Korean keywords.
            
            [RULES]
            1. **Entity Mapping**: Identify proper nouns (Idols, Groups, Agencies) and map them to their **official Korean stage names**.
            2. **No Literal Translation**: NEVER translate names literally.
               - Bad: 'G-Dragon' -> 'Giant Dragon' or 'G-Yong'
               - Good: 'G-Dragon' -> '지드래곤'
            3. **Group Names**: Use the Korean name most commonly found in news articles.
               - 'BTS' -> '방탄소년단'
               - 'Blackpink' -> '블랙핑크'
            4. Output ONLY the translated text without any explanation.
            """;

        ChatRequest request = ChatRequest.builder()
                .model(config.getChatModel())
                .temperature(0.0) // 일관된 결과를 위해 0.0 유지
                .messages(List.of(
                        ChatRequest.Message.builder()
                                .role("system")
                                .content(systemPrompt)
                                .build(),
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(query)
                                .build()
                ))
                .build();

        try {
            ChatResponse response = WebClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            if (response != null && !response.getChoices().isEmpty()) {
                String translated = response.getChoices().get(0).getMessage().getContent().trim();
                log.info("번역 결과: '{}' -> '{}'", query, translated);
                return translated;
            }
        } catch (Exception e) {
            log.error("검색어 번역 중 오류 발생. 원본 검색어 사용: {}", query, e);
        }

        return query;
    }

    private boolean isKorean(String text) {
        return text != null && KOREAN_PATTERN.matcher(text).matches();
    }

    /**
     * 1단계: 뉴스 원문 → GPT 요약
     */
    public String summarize(String content) {
        ChatRequest request = ChatRequest.builder()
                .model(config.getChatModel())
                .temperature(0.3)
                .maxTokens(500)
                .messages(List.of(
                        ChatRequest.Message.builder()
                                .role("system")
                                .content("당신은 뉴스 요약 전문가입니다. " +
                                        "인물 및 고유명사, 숫자는 모두 바꾸지말고 요약해주세요. " +
                                        "핵심 인물은 빼놓지 말고 객관적이고 명확하게 요약하세요. " +
                                        "다음 뉴스 기사의 핵심 내용을 가지고 4 ~ 6문장으로 요약해주세요.")
                                .build(),
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(content)
                                .build()
                ))
                .build();

        ChatResponse response = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .uri("https://api.openai.com/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .block();

        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * 2단계: 요약문 → 임베딩 벡터
     */
    public float[] embed(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model(config.getEmbeddingModel())
                .input(text)
                .build();

        EmbeddingResponse response = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .uri("https://api.openai.com/v1/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();

        List<Double> doubles = response.getData().get(0).getEmbedding();
        float[] result = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            result[i] = doubles.get(i).floatValue();
        }
        return result;
    }
}