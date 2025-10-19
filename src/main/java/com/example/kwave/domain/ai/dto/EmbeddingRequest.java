package com.example.kwave.domain.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmbeddingRequest {
    private String model;
    private String input;
}
