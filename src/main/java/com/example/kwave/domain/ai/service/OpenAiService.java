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

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final OpenAiConfig config;

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
                                        "다음 뉴스 기사의 핵심 내용을 가지고 3-5문장으로 요약해주세요.")
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