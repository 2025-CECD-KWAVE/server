package com.example.kwave.domain.ai.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsVectorDto {

    private String newsId;

    private String newsContent;

    private String newsSummary;

    private float[] embedding;
}
