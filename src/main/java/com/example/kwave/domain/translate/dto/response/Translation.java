package com.example.kwave.domain.translate.dto.response;

import lombok.Data;

@Data
public class Translation {
    private String detected_source_language;
    private String text;
}
