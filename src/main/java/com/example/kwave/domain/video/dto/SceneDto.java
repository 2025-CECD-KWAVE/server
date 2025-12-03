package com.example.kwave.domain.video.dto;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;
@Getter
@Setter
@NoArgsConstructor
public class SceneDto {

    private int sceneIndex;          // 장면 번호
    private String description;      // 장면 설명
    private String extraPrompt;      // 프롬프트 텍스트
    private String matchedImageUrl;  // 해당 장면과 매칭된 이미지 URL (없으면 null)

    @Override
    public String toString() {
        return "SceneDto{" +
                "sceneIndex=" + sceneIndex +
                ", description='" + description + '\'' +
                ", extraPrompt='" + extraPrompt + '\'' +
                ", matchedImageUrl='" + matchedImageUrl + '\'' +
                '}';
    }
}