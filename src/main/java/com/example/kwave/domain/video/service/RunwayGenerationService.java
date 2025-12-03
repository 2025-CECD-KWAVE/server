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
                "duration", 4
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

        for (int i = 0; i < 20; i++) {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

            ResponseEntity<Map> poll = restTemplate.exchange(
                    statusUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );
            Map<String, Object> pollBody = poll.getBody();
            if (pollBody == null) continue;

            String status = (String) pollBody.get("status");
            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                Object output = pollBody.get("output");
                if (output instanceof List<?> list && !list.isEmpty()) {
                    return list.get(0).toString();
                }
            }
        }
        throw new RuntimeException("Runway 영상 URL을 가져오지 못했습니다.");
    }
}
