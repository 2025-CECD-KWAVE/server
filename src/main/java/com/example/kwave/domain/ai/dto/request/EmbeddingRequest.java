package com.example.kwave.domain.ai.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmbeddingRequest {
    private String model;
    private String input;
}
