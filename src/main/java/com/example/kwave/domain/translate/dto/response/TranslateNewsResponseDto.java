package com.example.kwave.domain.translate.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TranslateNewsResponseDto {
    private List<Translation> translations;
}
