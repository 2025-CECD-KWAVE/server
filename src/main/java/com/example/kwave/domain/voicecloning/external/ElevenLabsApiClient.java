package com.example.kwave.domain.voicecloning.external;

import com.example.kwave.domain.voicecloning.dto.VoiceCloningRequestBody;
import com.example.kwave.domain.voicecloning.external.ElevenLabsApiProperties;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ElevenLabsApiClient {

    private final RestTemplate restTemplate;
    private final ElevenLabsApiProperties properties;

    private static final String API_BASE_URL = "https://api.elevenlabs.io/v1";

    public byte[] fetchVoiceCloning(String voiceId, String textToSpeak, String modelId, String outputFormat) {

        // 1. URL (쿼리 파라미터 포함)
        String url = String.format(
            "%s/text-to-speech/%s?output_format=%s",
            API_BASE_URL, voiceId, outputFormat
        );

        // 2. 헤더 설정 (API 키, Content-Type)
        HttpHeaders headers = new HttpHeaders();
        headers.set("xi-api-key", properties.getApiKey());
        headers.set("Content-Type", "application/json");
        headers.setAccept(List.of(MediaType.valueOf("audio/mpeg")));

        // 3. 본문(Body) 설정 (DTO 사용)
        VoiceCloningRequestBody body = new VoiceCloningRequestBody(textToSpeak, modelId);

        // 4. HttpEntity로 헤더와 본문 결합
        HttpEntity<VoiceCloningRequestBody> entity = new HttpEntity<>(body, headers);

        // 5. API 요청 실행
        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                url,
                entity,
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("ElevenLabs API 호출 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("ElevenLabs API 요청 중 오류 발생", e);
        }
    }
}