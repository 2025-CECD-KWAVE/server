package com.example.kwave.domain.voicecloning.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "elevenlabs") // 1. application.yml의 'elevenlabs'와 연결
public class ElevenLabsApiProperties {

    private String apiKey;
    
}