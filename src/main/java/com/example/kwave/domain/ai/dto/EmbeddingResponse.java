package com.example.kwave.domain.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class EmbeddingResponse {
    private List<EmbeddingData> data;

    @Data
    public static class EmbeddingData {
        private List<Double> embedding;
    }

}
