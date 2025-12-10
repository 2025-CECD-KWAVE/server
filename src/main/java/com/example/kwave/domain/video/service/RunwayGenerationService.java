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

    private static final String TEXT_TO_VIDEO_URL = "https://api.dev.runwayml.com/v1/text_to_video";
    private static final String IMAGE_TO_VIDEO_URL = "https://api.dev.runwayml.com/v1/image_to_video";

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(runwayApiKey);
        headers.add("X-Runway-Version", "2024-11-06");
        return headers;
    }

    public String generateTextToVideo(String prompt) {
        return sendRunwayRequest(TEXT_TO_VIDEO_URL, Map.of(
                "model", "veo3.1_fast",
                "promptText", prompt,
                "ratio", "1080:1920",
                "duration", 4
        ));
    }

    public String generateImageToVideo(String prompt, String imageUrl) {
        return sendRunwayRequest(IMAGE_TO_VIDEO_URL, Map.of(
                "model", "gen4_turbo",
                "promptImage", imageUrl,
                "promptText", prompt,
                "ratio", "720:1280",
                "duration", 5
        ));
    }

    private String sendRunwayRequest(String url, Map<String, Object> body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHeaders();

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class
        );

        Map<String, Object> result = response.getBody();
        if (result == null || !result.containsKey("id"))
            throw new RuntimeException("Runway 작업 생성 실패");

        String taskId = result.get("id").toString();
        String statusUrl = "https://api.dev.runwayml.com/v1/tasks/" + taskId;

        for (int i = 0; i < 40; i++) {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

            ResponseEntity<Map> poll = restTemplate.exchange(
                    statusUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );
            Map<String, Object> pollBody = poll.getBody();
            if (pollBody == null) continue;
            log.info("Runway Polling: {}", pollBody);


            String status = (String) pollBody.get("status");
            if ("FAILED".equalsIgnoreCase(status)) {
                String errorMsg = pollBody.getOrDefault("error", "Unknown error").toString();
                throw new RuntimeException("Runway 작업 실패: " + errorMsg);
            }
            if ("SUCCEEDED".equalsIgnoreCase(status)) {

                Object output = pollBody.get("output");

                // 🔥 [추가3] output이 Map 형태일 경우 (예: {"url": "...", "duration":5})
                if (output instanceof Map<?, ?> mapOutput) {
                    Object urlObj = mapOutput.get("url");
                    if (urlObj != null) {
                        return urlObj.toString();
                    }
                }

                // 🔥 [추가4] output이 List 형태일 경우 처리
                if (output instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);

                    // 리스트 내부가 Map이면 url 꺼내기
                    if (first instanceof Map<?, ?> firstMap) {
                        Object urlObj = firstMap.get("url");
                        if (urlObj != null) {
                            return urlObj.toString();
                        }
                    }

                    // 문자열 형태면 그대로 반환
                    return first.toString();
                }
            }

        }
        throw new RuntimeException("Runway 영상 URL을 가져오지 못했습니다.");
    }
}
