package com.example.kwave.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}