package com.example.kwave.domain.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneDto {
    private int sceneIndex;
    private String description;
    private String extraPrompt;
}