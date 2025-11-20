package com.example.kwave.domain.video.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RunwayGenerationService {

    @Value("${runway.api.key}")
    private String runwayApiKey;

    // ✅ Runway dev API의 올바른 엔드포인트
    private static final String RUNWAY_API_URL = "https://api.dev.runwayml.com/v1/text_to_video";

    /**
     * 텍스트 프롬프트로 영상 생성 요청 → 결과 URL 반환
     */
    public String generateVideo(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        if (runwayApiKey == null || runwayApiKey.isBlank()) {
            throw new IllegalStateException("Runway API 키가 설정되어 있지 않습니다. application.yml 확인 필요");
        }

        // ✅ 요청 본문 (Runway 공식 포맷)
        Map<String, Object> body = Map.of(
                "model", "veo3.1_fast",
                "promptText", prompt,
                "ratio", "1080:1920",
                "duration", 4
        );

        // ✅ 헤더 설정 (Runway dev API에 필수)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(runwayApiKey);
        headers.add("X-Runway-Version", "2024-11-06"); // ✅ 필수 버전 헤더

        try {
            // 1️⃣ Runway로 요청 전송
            ResponseEntity<Map> response = restTemplate.exchange(
                    RUNWAY_API_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result == null || !result.containsKey("id")) {
                throw new RuntimeException("Runway 작업 생성 실패: 응답이 비정상");
            }

            String taskId = result.get("id").toString();
            log.info("🚀 Runway 작업 생성 완료, Task ID={}", taskId);

            // 2️⃣ 상태 폴링용 URL (Runway dev API 규칙)
            String statusUrl = "https://api.dev.runwayml.com/v1/tasks/" + taskId;
            String videoUrl = null;

            for (int i = 0; i < 20; i++) {
                Thread.sleep(5000); // 5초마다 상태 확인

                ResponseEntity<Map> pollResponse = restTemplate.exchange(
                        statusUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                Map<String, Object> pollBody = pollResponse.getBody();
                String status = (String) pollBody.get("status");

                if ("SUCCEEDED".equalsIgnoreCase(status)) {
                    Object outputObj = pollBody.get("output");

                    if (outputObj instanceof List) {
                        List<?> outputList = (List<?>) outputObj;
                        if (!outputList.isEmpty() && outputList.get(0) instanceof String) {
                            videoUrl = (String) outputList.get(0);  // ✅ URL 바로 꺼내기
                            log.info("✅ Runway 영상 생성 완료: {}", videoUrl);
                        }
                    } else if (outputObj instanceof String) {
                        // 혹시라도 단일 문자열로 오는 경우 대비
                        videoUrl = (String) outputObj;
                        log.info("✅ Runway 영상 생성 완료: {}", videoUrl);
                    } else {
                        log.warn("⚠️ 예상치 못한 output 타입: {}", outputObj);
                    }

                    break;
                }

                log.info("⏳ Runway 상태: {}", status);
            }

            if (videoUrl == null) throw new RuntimeException("Runway 영상 URL을 가져오지 못했습니다.");
            return videoUrl;

        } catch (Exception e) {
            log.error("❌ Runway API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Runway API 호출 실패", e);
        }
    }
}
