package com.example.kwave.domain.voicecloning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCloningService {

    @Value("${app.voice-url}")
    private String voiceServiceUrl;

    private final RestTemplate restTemplate;

    public void streamAsWav(String language, String fullText, OutputStream clientOutputStream) {
        String[] paragraphs = fullText.split("\\n\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) continue;

            try {
                byte[] wavData = restTemplate.postForObject(
                        voiceServiceUrl,
                        Map.of("language", language, "text", paragraph.trim()),
                        byte[].class
                );

                if (wavData == null || wavData.length < 44) continue;

                clientOutputStream.write(wavData);
                clientOutputStream.write("\n--END--\n".getBytes(StandardCharsets.UTF_8));
                clientOutputStream.flush();

            } catch (Exception e) {
                log.error("WAV 전송 실패", e);
            }
        }
    }

}
